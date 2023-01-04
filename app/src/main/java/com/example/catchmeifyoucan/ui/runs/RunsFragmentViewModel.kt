package com.example.catchmeifyoucan.ui.runs

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class RunsFragmentViewModel @Inject constructor(private val runsRepository: RunsRepository): ViewModel() {

    companion object {
        private val TAG = RunsFragmentViewModel::class.java.simpleName
    }

    fun getAllUserRuns(): Single<List<RunsModel>> {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        return runsRepository.getAllRuns(uid)
    }

    fun getUserRun(uid: String, runId: String): Single<RunsModel> {
        return runsRepository.getRun(uid, runId)
    }
}