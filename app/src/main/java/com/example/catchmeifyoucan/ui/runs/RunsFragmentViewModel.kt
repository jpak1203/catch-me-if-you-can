package com.example.catchmeifyoucan.ui.runs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class RunsFragmentViewModel @Inject constructor(private val runsRepository: RunsRepository): ViewModel() {

    companion object {
        private val TAG = RunsFragmentViewModel::class.java.simpleName
    }

    private val _runDetails = MutableLiveData<RunsModel>()
    val runDetails: LiveData<RunsModel>
        get() = _runDetails

    private val _runTime = MutableLiveData<Int>()
    val runTime: LiveData<Int>
        get() = _runTime

    var seconds = 0

    fun getAllUserRuns(): Single<List<RunsModel>> {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        return runsRepository.getAllRuns(uid)
    }

    fun getUserRun(uid: String, runId: String): Single<RunsModel> {
        return runsRepository.getRun(uid, runId)
    }

    fun setRunDetails(runDetails: RunsModel) {
        _runDetails.postValue(runDetails)
    }
}