package com.example.catchmeifyoucan.auth

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import timber.log.Timber

class LoginFragment : Fragment() {

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }

    private val viewModel by viewModels<LoginFragmentViewModel>()
    private lateinit var binding: FragmentLoginBinding

    private val registerForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.checkResultAndExecute {
            findNavController().navigate(R.id.home_fragment)
        }.onFailure { e -> Timber.e(TAG, "Error: ${e.message}") }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        binding.loginButton.setOnClickListener { launchSignInFlow() }

        initView()
        return binding.root
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        registerForResult.launch(signInIntent)
    }

    private fun ActivityResult.checkResultAndExecute(block: ActivityResult.() -> Unit) =
        if (resultCode == RESULT_OK) runCatching(block)
        else Result.failure(Exception("Something went wrong"))

    private fun initView() {
        binding.signupButton.setOnClickListener {
            findNavController().navigate(R.id.signup_fragment)
        }
    }

}