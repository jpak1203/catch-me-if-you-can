package com.example.catchmeifyoucan.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.utils.ValidatorUtil
import javax.inject.Inject

class SignupFragmentViewModel @Inject constructor(): ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String>
        get() = _confirmPassword

    var emailLostFocus = false
    var passwordLostFocus = false
    var confirmPasswordLostFocus = false

    fun setEmail(emailAddress: String) {
        _email.postValue(emailAddress)
    }

    fun setPassword(password: String) {
        _password.postValue(password)
    }

    fun setConfirmPassword(password: String) {
        _confirmPassword.postValue(password)
    }

    fun showEmailError(): Boolean {
        return emailLostFocus && !ValidatorUtil.validEmailAddress(email.value)
    }

    fun showPasswordError(): Boolean {
        return passwordLostFocus
                && !ValidatorUtil.validPassword(password.value)
    }

    fun showConfirmPasswordError(): Boolean {
        return confirmPasswordLostFocus
                && !ValidatorUtil.validConfirmPassword(password.value, confirmPassword.value)
    }

    fun validateSignup(): Boolean {
        return ValidatorUtil.validPassword(password.value)
                && ValidatorUtil.validEmailAddress(email.value)
                && ValidatorUtil.validConfirmPassword(password.value, confirmPassword.value)
    }


}