package com.example.catchmeifyoucan.utils

import android.content.res.ColorStateList
import android.util.Patterns
import androidx.core.content.ContextCompat
import com.example.catchmeifyoucan.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object ValidatorUtil {

    private val TAG = ValidatorUtil::class.java.simpleName

    private const val MINIMUM_PASSWORD_LENGTH = 8
    private val PASSWORD_CONTENT_REQUIREMENTS = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%&*()_+=|<>?{}\\\\[\\\\]~-]).+$")


    fun validPassword(password: String?): Boolean {
        return if (password.isNullOrBlank() || password.isNullOrEmpty()) {
            false
        } else {
            password.length >= MINIMUM_PASSWORD_LENGTH
                    && password.matches(PASSWORD_CONTENT_REQUIREMENTS)
        }
    }

    fun validEmailAddress(emailAddress: String?): Boolean {
        return if (emailAddress.isNullOrBlank() || emailAddress.isNullOrEmpty()) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
        }
    }

    fun validConfirmPassword(password: String?, confirmPassword: String?): Boolean {
        return !confirmPassword.isNullOrEmpty() && password.equals(confirmPassword)
    }

    fun setEditTextErrorState(textInputEditText: TextInputEditText,
                              editTextInputLayout: TextInputLayout,
                              error: Boolean,
                              errorMessage: String ?= "") {
        val context = textInputEditText.context
        if (error) {
            textInputEditText.setHintTextColor(ContextCompat.getColor(context, R.color.cinnabar))
            editTextInputLayout.error = errorMessage
            editTextInputLayout.defaultHintTextColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cinnabar))
        } else {
            textInputEditText.setHintTextColor(ContextCompat.getColor(context, R.color.black))
            editTextInputLayout.defaultHintTextColor =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black))
            editTextInputLayout.error = null
        }
    }
}