package com.example.catchmeifyoucan.ui.account

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentDeleteAccountBinding
import com.example.catchmeifyoucan.rxjava.LoadingDialog
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.ui.StartupFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class DeleteAccountFragment : BaseFragment() {

    companion object {
        private val TAG = DeleteAccountFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AccountFragmentViewModel
    private lateinit var binding: FragmentDeleteAccountBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
        viewModel = ViewModelProvider(this, viewModelFactory)[AccountFragmentViewModel::class.java]

        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as HomeActivity).showToolbar("")
    }

    private fun initView() {
        binding.deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_account))
                .setMessage(getString(R.string.delete_account_message))
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.delete) { _, _ ->
                    deleteAccount()
                }
                .show()
        }
    }

    private fun deleteAccount() {
        viewModel.deleteUser()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribeWith(object : LoadingDialog<Unit>(requireActivity()) {
                override fun onSuccess(t: Unit) {
                    super.onSuccess(t)
                    val user = FirebaseAuth.getInstance().currentUser
                    val storage = FirebaseStorage.getInstance().reference
                    val userProfileRef = storage.child(user!!.uid)
                    userProfileRef.listAll().addOnCompleteListener {
                        if (it.isSuccessful) {
                            it.result.items.forEach { ref ->
                                ref.delete()
                            }
                        }
                    }
                    FirebaseAuth.getInstance().currentUser!!.delete().addOnCompleteListener {
                        if (it.isSuccessful) {
                            val bundle = bundleOf(StartupFragment.FROM_ACCOUNT_DELETED to true)
                            findNavController().navigate(R.id.action_delete_account_fragment_to_startup_fragment, bundle)
                        } else {
                            Timber.e( it.exception)
                        }
                    }
                }
            })
    }
}