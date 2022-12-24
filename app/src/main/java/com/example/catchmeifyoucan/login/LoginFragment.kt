package com.example.catchmeifyoucan.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.catchmeifyoucan.activities.MainActivity
import com.example.catchmeifyoucan.databinding.FragmentLoginBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import timber.log.Timber

class LoginFragment : Fragment() {

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }

    private val viewModel by viewModels<LoginFragmentViewModel>()
    private lateinit var binding: FragmentLoginBinding

    private val registerForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.checkResultAndExecute {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
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

        binding.authButton.setOnClickListener { launchSignInFlow() }

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
            .build()

        registerForResult.launch(signInIntent)
    }

    private fun ActivityResult.checkResultAndExecute(block: ActivityResult.() -> Unit) =
        if (resultCode == RESULT_OK) runCatching(block)
        else Result.failure(Exception("Something went wrong"))

}