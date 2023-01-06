package com.example.catchmeifyoucan.ui.account

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountFragmentViewModel @Inject constructor(): ViewModel() {

    companion object {
        private val TAG = AccountFragmentViewModel::class.java.simpleName
    }

    var firstName = ""
    var lastName = ""
    var saveAttempted = false

    fun validForm(): Boolean {
        return firstName.isNotBlank()
                && lastName.isNotBlank()
    }

    fun saveName(): Task<Void> {
        val builder = UserProfileChangeRequest.Builder()
        builder.displayName = "$firstName $lastName"
        return FirebaseAuth.getInstance().currentUser!!.updateProfile(builder.build())
    }

    fun deleteUser() {
        FirebaseAuth.getInstance().currentUser?.delete()
    }

    fun updateUser() {
        val builder = UserProfileChangeRequest.Builder()
        builder.displayName = FirebaseAuth.getInstance().currentUser!!.displayName
        FirebaseAuth.getInstance().currentUser!!.updateProfile(builder.build())
    }

    fun setUserProfile(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        val storage = FirebaseStorage.getInstance().reference
        val userProfileRef = storage.child("${user!!.uid}/profile.jpg")
        val uploadTask = userProfileRef.putFile(uri)
        uploadTask.addOnFailureListener {
            Timber.e(it)
        }.addOnSuccessListener {
            Timber.i("Upload successful")
            user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
        }
    }

}