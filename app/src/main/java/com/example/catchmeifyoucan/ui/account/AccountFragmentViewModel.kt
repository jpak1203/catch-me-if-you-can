package com.example.catchmeifyoucan.ui.account

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.ui.auth.UserRepository
import com.example.catchmeifyoucan.utils.ValidatorUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.rxjava3.core.Single
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountFragmentViewModel @Inject constructor(private val userRepository: UserRepository): ViewModel() {

    companion object {
        private val TAG = AccountFragmentViewModel::class.java.simpleName
    }

    private val _password = MutableLiveData<String>()
    val password: LiveData<String>
        get() = _password

    private val _confirmPassword = MutableLiveData<String>()
    val confirmPassword: LiveData<String>
        get() = _confirmPassword

    var firstName = ""
    var lastName = ""
    var saveAttempted = false
    var passwordLostFocus = false
    var confirmPasswordLostFocus = false

    fun validForm(): Boolean {
        return firstName.isNotBlank()
                && lastName.isNotBlank()
    }

    fun validateChangePassword(): Boolean {
        return ValidatorUtil.validPassword(password.value)
                && ValidatorUtil.validConfirmPassword(password.value, confirmPassword.value)
    }

    fun showPasswordError(): Boolean {
        return passwordLostFocus
                && !ValidatorUtil.validPassword(password.value)
    }

    fun showConfirmPasswordError(): Boolean {
        return confirmPasswordLostFocus
                && !ValidatorUtil.validConfirmPassword(password.value, confirmPassword.value)
    }

    fun setPassword(password: String) {
        _password.postValue(password)
    }

    fun setConfirmPassword(password: String) {
        _confirmPassword.postValue(password)
    }

    fun saveName(): Task<Void> {
        val builder = UserProfileChangeRequest.Builder()
        builder.displayName = "$firstName $lastName"
        return FirebaseAuth.getInstance().currentUser!!.updateProfile(builder.build())
    }

    fun deleteUser(): Single<Unit> {
        return userRepository.deleteUser(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    fun updateUser(): Task<Void> {
        val builder = UserProfileChangeRequest.Builder()
        builder.displayName = FirebaseAuth.getInstance().currentUser!!.displayName
        return FirebaseAuth.getInstance().currentUser!!.updateProfile(builder.build())
    }

    fun updatePassword(): Task<Void> {
        return FirebaseAuth.getInstance().currentUser!!.updatePassword(_password.value!!)
    }

    fun setUserProfile(uri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        val storage = FirebaseStorage.getInstance().reference
        val userProfileRef = storage.child("${user!!.uid}/profile.jpg")
        val uploadTask = userProfileRef.putFile(uri)
        uploadTask.addOnFailureListener {
            Timber.e(it)
        }.addOnSuccessListener {
            userProfileRef.downloadUrl.addOnCompleteListener {
                if (it.isSuccessful) {
                    user.updateProfile(UserProfileChangeRequest.Builder().setPhotoUri(it.result).build())
                } else {
                    Timber.e(it.exception)
                }
            }
        }
    }

}