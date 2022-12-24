package com.example.catchmeifyoucan.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.catchmeifyoucan.R
import com.google.firebase.auth.FirebaseAuth

class MainActivityViewModel: ViewModel() {

    fun getNavigationStartDestination(): Int {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            R.id.home_fragment
        } else {
            R.id.login_fragment
        }
    }
}