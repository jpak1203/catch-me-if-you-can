package com.example.catchmeifyoucan.utils

import android.content.Context
import android.content.res.ColorStateList
import android.util.Patterns
import android.widget.Toast
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

    fun firebaseErrorCheck(context: Context, errorCode: String) {
        when (errorCode) {
            "ERROR_INVALID_CUSTOM_TOKEN" -> Toast.makeText(
                context,
                "The custom token format is incorrect. Please check the documentation.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_CUSTOM_TOKEN_MISMATCH" -> Toast.makeText(
                context,
                "The custom token corresponds to a different audience.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_INVALID_CREDENTIAL" -> Toast.makeText(
                context,
                "The supplied auth credential is malformed or has expired.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_INVALID_EMAIL" -> {
                Toast.makeText(
                    context,
                    "The email address is badly formatted.",
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_WRONG_PASSWORD" -> {
                Toast.makeText(
                    context,
                    "The password is invalid or the user does not have a password.",
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_USER_MISMATCH" -> Toast.makeText(
                context,
                "The supplied credentials do not correspond to the previously signed in user.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_REQUIRES_RECENT_LOGIN" -> Toast.makeText(
                context,
                "This operation is sensitive and requires recent authentication. Log in again before retrying this request.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> Toast.makeText(
                context,
                "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_EMAIL_ALREADY_IN_USE" -> {
                Toast.makeText(
                    context,
                    "The email address is already in use by another account.   ",
                    Toast.LENGTH_LONG
                ).show()
            }
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> Toast.makeText(
                context,
                "This credential is already associated with a different user account.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_USER_DISABLED" -> Toast.makeText(
                context,
                "The user account has been disabled by an administrator.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_USER_TOKEN_EXPIRED" -> Toast.makeText(
                context,
                "The user's credential is no longer valid. The user must sign in again.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_USER_NOT_FOUND" -> Toast.makeText(
                context,
                "There is no user record corresponding to this identifier. The user may have been deleted.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_INVALID_USER_TOKEN" -> Toast.makeText(
                context,
                "The user\\'s credential is no longer valid. The user must sign in again.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_OPERATION_NOT_ALLOWED" -> Toast.makeText(
                context,
                "This operation is not allowed. You must enable this service in the console.",
                Toast.LENGTH_LONG
            ).show()
            "ERROR_WEAK_PASSWORD" -> {
                Toast.makeText(
                    context,
                    "The given password is invalid.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}