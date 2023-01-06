package com.example.catchmeifyoucan.activities

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.R
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class HomeActivityViewModel @Inject constructor(): ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String>
        get() = _email

    private val _profilePic = MutableLiveData<Uri>()
    val profilePic: LiveData<Uri>
        get() = _profilePic

    fun setUserEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            _email.postValue(user.email)
        } else {
            _email.postValue("")
        }
    }

    fun setUserProfilePic() {
        val user = FirebaseAuth.getInstance().currentUser
        _profilePic.postValue(user!!.photoUrl)
    }

    fun getNavigationStartDestination(): Int {
        return if (FirebaseAuth.getInstance().currentUser != null) {
            R.id.home_fragment
        } else {
            R.id.startup_fragment
        }
    }
}