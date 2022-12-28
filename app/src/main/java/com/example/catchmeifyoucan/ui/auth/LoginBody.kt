package com.example.catchmeifyoucan.ui.auth

data class LoginBody(private val email: String, private val password: String) {
    companion object {
        private val TAG = LoginBody::class.java.simpleName
    }
}
