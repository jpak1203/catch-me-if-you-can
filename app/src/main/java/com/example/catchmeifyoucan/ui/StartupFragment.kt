package com.example.catchmeifyoucan.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.FragmentStartupBinding
import dagger.android.support.AndroidSupportInjection

class StartupFragment : Fragment() {

    companion object {
        private val TAG = StartupFragment::class.java.simpleName
        const val FROM_ACCOUNT_DELETED = "account_deleted_arg"
    }

    private lateinit var binding: FragmentStartupBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartupBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    private fun initView() {
        if (arguments?.getBoolean(FROM_ACCOUNT_DELETED) == true) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.delete_account_success_title))
                .setMessage(getString(R.string.delete_account_success_message))
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            arguments?.putBoolean(FROM_ACCOUNT_DELETED, false)
        }
        binding.signupButton.setOnClickListener {
            findNavController().navigate(R.id.signup_fragment)
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.login_fragment)
        }
    }
}