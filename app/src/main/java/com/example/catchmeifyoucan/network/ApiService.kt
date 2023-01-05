package com.example.catchmeifyoucan.network

import com.example.catchmeifyoucan.ui.auth.UserModel
import com.example.catchmeifyoucan.ui.runs.RunsModel
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

    @GET("/users/{uid}/runs/{runId}.json")
    fun getUserRun(
        @Path("uid") uid: String,
        @Path("runId") runId: String
    ): Single<RunsModel>

    @PUT("/users/{uid}/runs/{runId}.json")
    fun saveUserRun(
        @Path("uid") uid: String,
        @Path("runId") runId: String,
        @Body runBody: RunsModel
    ): Single<Unit>

    @GET("/users/{uid}/runs.json")
    fun getAllUserRuns(
        @Path("uid") uid: String,
    ): Single<Map<String, RunsModel>>

}