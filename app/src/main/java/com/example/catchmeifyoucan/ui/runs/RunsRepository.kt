package com.example.catchmeifyoucan.ui.runs

import com.example.catchmeifyoucan.network.ApiService
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class RunsRepository @Inject constructor(private val api: ApiService) {



    init {
        val user = FirebaseAuth.getInstance().currentUser
        if (FirebaseAuth.getInstance().currentUser != null) {
        }
    }

    fun saveRun(uid: String, id: String, value: RunsModel): Single<Unit> {
        return api.saveUserRun(uid, id, value)
    }

}