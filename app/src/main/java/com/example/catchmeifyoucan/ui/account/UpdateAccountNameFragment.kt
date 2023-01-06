package com.example.catchmeifyoucan.ui.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.FragmentUpdateAccountNameBinding
import com.example.catchmeifyoucan.ui.BaseFragment
import com.example.catchmeifyoucan.utils.ValidatorUtil.setEditTextErrorState
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class UpdateAccountNameFragment: BaseFragment() {

    companion object {
        private val TAG = UpdateAccountNameFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AccountFragmentViewModel
    private lateinit var binding: FragmentUpdateAccountNameBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUpdateAccountNameBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this, viewModelFactory)[AccountFragmentViewModel::class.java]
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        initView()
        return binding.root
    }

    private fun initView() {
        binding.firstNameEditText.addTextChangedListener {
            viewModel.firstName = it.toString()
            if (viewModel.saveAttempted) setFirstNameErrorState()
        }
        binding.lastNameEditText.addTextChangedListener {
            viewModel.lastName = it.toString()
            if (viewModel.saveAttempted) setLastNameErrorState()
        }
        binding.saveAndContinueMaterialButton.setOnClickListener {
            viewModel.saveAttempted = true
            if (viewModel.validForm()) {
                viewModel.saveName().addOnCompleteListener {
                    viewModel.firstName = ""
                    viewModel.lastName = ""
                    findNavController().navigateUp()
                }
            }
            setFirstNameErrorState()
            setLastNameErrorState()
        }
    }

    private fun setFirstNameErrorState(error: Boolean = viewModel.firstName.isBlank()) {
        val errorText = if (error) getString(R.string.required_field) else null
        setEditTextErrorState(binding.firstNameEditText, binding.firstNameTextInputLayout, error, errorText)
    }

    private fun setLastNameErrorState(error: Boolean = viewModel.lastName.isBlank()) {
        val errorText = if (error) getString(R.string.required_field) else null
        setEditTextErrorState(binding.lastNameEditText, binding.lastNameTextInputLayout, error, errorText)
    }
}