package com.example.catchmeifyoucan.ui.runs

import com.example.catchmeifyoucan.dao.AppDatabase
import com.example.catchmeifyoucan.network.ApiService
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.toObservable
import timber.log.Timber
import javax.inject.Inject

class RunsRepository @Inject constructor(private val api: ApiService,
                                         private val appDatabase: AppDatabase) {

    fun saveRun(uid: String, id: String, value: RunsModel): Single<Unit> {
        return api.saveUserRun(uid, id, value)
    }

    fun getAllRuns(uid: String): Single<List<RunsModel>> {
        return api.getAllUserRuns(uid).flatMap {
            Single.just(it.values.toList())
        }
    }

    fun getRun(uid: String, runId: String): Single<RunsModel> {
        return api.getUserRun(uid, runId)
    }

}