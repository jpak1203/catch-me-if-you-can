package com.example.catchmeifyoucan.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.R
import com.google.firebase.auth.FirebaseAuth

class HomeActivityViewModel: ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    fun setUserEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            _email.postValue(user.email)
        } else {
            _email.postValue("")
        }
    }

    fun getNavigationStartDestination(): Int {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            R.id.home_fragment
        } else {
            R.id.startup_fragment
        }
    }
}