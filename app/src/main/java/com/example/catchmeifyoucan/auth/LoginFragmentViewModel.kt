package com.example.catchmeifyoucan.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.utils.ValidatorUtil

class LoginFragmentViewModel: ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    var emailLostFocus = false
    var passwordLostFocus = false

    fun setEmail(emailAddress: String) {
        _email.postValue(emailAddress)
    }

    fun setPassword(password: String) {
        _password.postValue(password)
    }

    fun showEmailError(): Boolean {
        return emailLostFocus && !ValidatorUtil.validEmailAddress(email.value)
    }

    fun showPasswordError(): Boolean {
        return passwordLostFocus
                && !ValidatorUtil.validPassword(password.value)
    }

    fun validateSignup(): Boolean {
        return ValidatorUtil.validPassword(password.value)
                && ValidatorUtil.validEmailAddress(email.value)
    }


}