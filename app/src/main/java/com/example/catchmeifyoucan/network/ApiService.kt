package com.example.catchmeifyoucan.network

import com.example.catchmeifyoucan.dao.Runs
import com.example.catchmeifyoucan.ui.auth.UserModel
import io.reactivex.rxjava3.core.Single
import retrofit2.http.*

interface ApiService {

    companion object {
        private val TAG = ApiService::class.java.simpleName
    }

    @PUT("/users/{uid}.json")
    fun createUser(
        @Path("uid") uid: String,
        @Body email: UserModel
    ): Single<Unit>

    @GET("/users/{userId}/runs.json")
    fun getUserRuns(
        @Path("userId") userId: String
    ): Single<Runs>

}