package com.example.catchmeifyoucan.ui.account

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.catchmeifyoucan.BuildConfig
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentAccountBinding
import com.example.catchmeifyoucan.ui.BaseFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class AccountFragment: BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = AccountFragment::class.java.simpleName
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.all {
            when (it.key.replace("android.permission.", "")) {
                getString(R.string.camera_permissions) -> {
                    if (it.value) {
                        pickFromCamera()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.camera_required_error, Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {
                            requestCameraPermissions()
                        }.show()
                    }
                }
                getString(R.string.storage_permissions) -> {
                    if (it.value) {
                        pickFromGallery()
                    } else {
                        Snackbar.make(
                            requireView(),
                            R.string.gallery_required_error, Snackbar.LENGTH_INDEFINITE
                        ).setAction(android.R.string.ok) {
                            requestStoragePermissions()
                        }.show()
                    }
                }
            }
            it.value
        }
    }

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                binding.settingProfileImage.setImageURI(uri)
                viewModel.setUserProfile(uri)
            }
        }
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.settingProfileImage.setImageURI(uri)
            viewModel.setUserProfile(uri)
        }
    }

    private var latestTmpUri: Uri? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AccountFragmentViewModel
    private lateinit var binding: FragmentAccountBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel = ViewModelProvider(this, viewModelFactory)[AccountFragmentViewModel::class.java]

        initView()
        subscribe()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as HomeActivity).showToolbar(getString(R.string.account))
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as HomeActivity).setUserProfilePic()
    }

    private fun initView() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user!!.photoUrl != null) {
            Glide.with(requireContext())
                .load(user!!.photoUrl)
                .into(binding.settingProfileImage)
        }
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.settingProfileImage.setOnClickListener {
            showImagePicDialog()
        }
        binding.editButtonTextView.setOnClickListener {
            findNavController().navigate(R.id.update_account_name_fragment)
        }
        binding.changePasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.change_password_fragment)
        }
        binding.deleteAccountTextView.setOnClickListener {
            findNavController().navigate(R.id.delete_account_fragment)
        }
    }

    private fun subscribe() {
        val user = FirebaseAuth.getInstance().currentUser
        binding.editButtonTextView.text =  if (!user!!.displayName.isNullOrEmpty()) {
            binding.nameField.text = user.displayName
            getString(R.string.edit)
        } else {
            binding.nameField.text = getString(R.string.please_add_name)
            getString(R.string.add)
        }
    }

    override fun onRefresh() {
        viewModel.updateUser().addOnCompleteListener {
            if (it.isSuccessful) binding.swipeRefreshLayout.isRefreshing = false
            else Timber.e(getString(R.string.error_getting_user_info))
        }
    }

    private fun requestCameraPermissions() {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
    }

    private fun requestStoragePermissions() {
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun showImagePicDialog() {
        val options = arrayOf(getString(R.string.camera), getString(R.string.gallery))
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.pick_image_title))
            .setItems(options) { _, which ->
                if (which == 0) {
                    requestCameraPermissions()
                } else if (which == 1) {
                    requestStoragePermissions()
                }
            }
            .show()
    }

    private fun pickFromCamera() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun pickFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(requireActivity().applicationContext, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

}