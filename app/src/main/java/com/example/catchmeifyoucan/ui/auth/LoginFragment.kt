package com.example.catchmeifyoucan.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.FragmentLoginBinding
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.utils.ValidatorUtil
import com.example.catchmeifyoucan.utils.ValidatorUtil.setEditTextErrorState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import timber.log.Timber
import javax.inject.Inject


class LoginFragment : BaseFragment() {

    companion object {
        private val TAG = LoginFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginFragmentViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginFragmentViewModel::class.java]
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
        auth = FirebaseAuth.getInstance()

        subscribe()
        initView()
        return binding.root
    }

    private fun subscribe() {
        viewModel.email.observe(viewLifecycleOwner) {
            setEmailErrorState(viewModel.showEmailError())
        }
        viewModel.password.observe(viewLifecycleOwner) {
            setPasswordErrorState(viewModel.showPasswordError())
        }
    }

    private fun initView() {
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { viewModel.emailLostFocus = true }
            setEmailErrorState(viewModel.showEmailError())
        }
        binding.emailEditText.addTextChangedListener { viewModel.setEmail(it.toString()) }

        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { viewModel.passwordLostFocus = true }
            setPasswordErrorState(viewModel.showPasswordError())
        }
        binding.passwordEditText.addTextChangedListener { viewModel.setPassword(it.toString()) }

        binding.forgotPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.forgot_password_fragment)
        }

        binding.loginButton.setOnClickListener {
            if (!viewModel.validateSignup()) {
                viewModel.emailLostFocus = true
                setEmailErrorState(viewModel.showEmailError())
                viewModel.passwordLostFocus = true
                setPasswordErrorState(viewModel.showPasswordError())
            } else {
                signIn(viewModel.email.value!!, viewModel.password.value!!)
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.i(TAG, "signInWithEmail:success")
                    findNavController().navigate(R.id.action_login_fragment_to_home_fragment)
                } else {
                    Timber.e(TAG, "signInWithEmail:failure", task.exception)
                    val errorCode = (task.exception as FirebaseAuthException).errorCode
                    ValidatorUtil.firebaseErrorCheck(requireContext(), errorCode)
                }
            }
    }

    private fun setEmailErrorState(error: Boolean) {
        val errorMessage =
            if (error) {
                if (binding.emailEditText.text.isNullOrEmpty()) {
                    getString(R.string.this_is_a_required_field)
                } else {
                    getString(R.string.email_invalid_message)
                }
            } else null
        setEditTextErrorState(
            binding.emailEditText, binding.emailTextInputLayout, error, errorMessage
        )
    }

    private fun setPasswordErrorState(error: Boolean) {
        val errorMessage = if (error) getString(R.string.incorrect_password_message) else null
        setEditTextErrorState(
            binding.passwordEditText, binding.passwordTextInputLayout, error, errorMessage
        )
    }

}