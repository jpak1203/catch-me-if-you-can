package com.example.catchmeifyoucan.ui.auth

import com.example.catchmeifyoucan.network.ApiService
import com.firebase.ui.auth.data.model.User
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: ApiService) {

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    var userBehaviorSubject: BehaviorSubject<User> = BehaviorSubject.create()

    fun createNewUser(uid: String, user: UserModel): Single<Unit> {
        return api.createUser(uid, user)
    }


}