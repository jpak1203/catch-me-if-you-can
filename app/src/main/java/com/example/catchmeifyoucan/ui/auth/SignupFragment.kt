package com.example.catchmeifyoucan.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.FragmentSignupBinding
import com.example.catchmeifyoucan.rxjava.LoadingDialog
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.utils.ValidatorUtil
import com.example.catchmeifyoucan.utils.ValidatorUtil.setEditTextErrorState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class SignupFragment : BaseFragment() {

    companion object {
        private val TAG = SignupFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSignupBinding
    private lateinit var viewModel: SignupFragmentViewModel
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[SignupFragmentViewModel::class.java]
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
        viewModel.confirmPassword.observe(viewLifecycleOwner) {
            setConfirmPasswordState(viewModel.showConfirmPasswordError())
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

        binding.confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { viewModel.confirmPasswordLostFocus = true }
            setConfirmPasswordState(viewModel.showConfirmPasswordError())
        }
        binding.confirmPasswordEditText.addTextChangedListener {
            viewModel.setConfirmPassword(it.toString())
        }

        binding.signupButton.setOnClickListener {
            if (!viewModel.validateSignup()) {
                viewModel.emailLostFocus = true
                setEmailErrorState(viewModel.showEmailError())
                viewModel.passwordLostFocus = true
                setPasswordErrorState(viewModel.showPasswordError())
                viewModel.confirmPasswordLostFocus = true
                setConfirmPasswordState(viewModel.showConfirmPasswordError())
            } else {
                createAccount(viewModel.email.value!!, viewModel.password.value!!)
            }
        }

        binding.loginNavText.setOnClickListener {
            findNavController().navigate(R.id.login_fragment)
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.i(TAG, "createUserWithEmail:success")
                    viewModel.createAccount(task.result.user!!.uid)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(object : LoadingDialog<Unit>(requireActivity()) {
                            override fun onError(e: Throwable) {
                                super.onError(e)
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(getString(R.string.error))
                                    .setMessage(getString(R.string.create_account_error_message))
                                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .show()
                            }
                        })
                    findNavController().navigate(R.id.action_signup_fragment_to_home_fragment)
                } else {
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
        val errorMessage = if (error) getString(R.string.password_invalid_message) else null
        setEditTextErrorState(
            binding.passwordEditText, binding.passwordTextInputLayout, error, errorMessage
        )
    }

    private fun setConfirmPasswordState(error: Boolean) {
        val errorMessage = if (error) getString(R.string.confirm_password_invalid_message) else null
        setEditTextErrorState(
            binding.confirmPasswordEditText, binding.confirmPasswordTextInputLayout, error, errorMessage
        )
    }
}