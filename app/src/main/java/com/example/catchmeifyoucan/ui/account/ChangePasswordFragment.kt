package com.example.catchmeifyoucan.ui.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.FragmentChangePasswordBinding
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.utils.ValidatorUtil.setEditTextErrorState
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class ChangePasswordFragment: BaseFragment() {

    companion object {
        private val TAG = ChangePasswordFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AccountFragmentViewModel
    private lateinit var binding: FragmentChangePasswordBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[AccountFragmentViewModel::class.java]
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        initView()
        subscribe()
        return binding.root
    }

    private fun initView() {
        binding.changePasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { viewModel.passwordLostFocus = true }
            setPasswordErrorState(viewModel.showPasswordError())
        }
        binding.changePasswordEditText.addTextChangedListener { viewModel.setPassword(it.toString()) }

        binding.confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { viewModel.confirmPasswordLostFocus = true }
            setConfirmPasswordState(viewModel.showConfirmPasswordError())
        }
        binding.confirmPasswordEditText.addTextChangedListener {
            viewModel.setConfirmPassword(it.toString())
        }

        binding.saveAndContinueMaterialButton.setOnClickListener {
            viewModel.saveAttempted = true
            if (!viewModel.validateChangePassword()) {
                viewModel.passwordLostFocus = true
                setPasswordErrorState(viewModel.showPasswordError())
                viewModel.confirmPasswordLostFocus = true
                setConfirmPasswordState(viewModel.showConfirmPasswordError())
            } else {
                viewModel.updatePassword().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "Successfully changed password!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    else Timber.e("Could not change password")
                }
            }
        }
    }

    private fun subscribe() {
        viewModel.password.observe(viewLifecycleOwner) {
            setPasswordErrorState(viewModel.showPasswordError())
        }
        viewModel.confirmPassword.observe(viewLifecycleOwner) {
            setConfirmPasswordState(viewModel.showConfirmPasswordError())
        }
    }

    private fun setPasswordErrorState(error: Boolean) {
        val errorMessage = if (error) getString(R.string.password_invalid_message) else null
        setEditTextErrorState(
            binding.changePasswordEditText, binding.changePasswordInputLayout, error, errorMessage
        )
    }

    private fun setConfirmPasswordState(error: Boolean) {
        val errorMessage = if (error) getString(R.string.confirm_password_invalid_message) else null
        setEditTextErrorState(
            binding.confirmPasswordEditText, binding.confirmPasswordInputLayout, error, errorMessage
        )
    }
}