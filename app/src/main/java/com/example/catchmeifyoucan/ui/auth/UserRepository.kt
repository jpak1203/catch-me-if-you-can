package com.example.catchmeifyoucan.ui.auth

import com.example.catchmeifyoucan.network.ApiService
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: ApiService) {

    companion object {
        private val TAG = UserRepository::class.java.simpleName
    }

    fun createNewUser(uid: String, user: UserModel): Single<Unit> {
        return api.createUser(uid, user)
    }

    fun deleteUser(uid: String): Single<Unit> {
        return api.deleteUser(uid)
    }


}