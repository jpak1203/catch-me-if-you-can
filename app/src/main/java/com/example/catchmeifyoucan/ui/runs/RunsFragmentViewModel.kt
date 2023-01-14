package com.example.catchmeifyoucan.ui.runs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catchmeifyoucan.dao.RunsDataSource
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RunsFragmentViewModel @Inject constructor(private val runsRepository: RunsRepository,
                                                private val dataSource: RunsDataSource): ViewModel() {

    companion object {
        private val TAG = RunsFragmentViewModel::class.java.simpleName
    }

    private val _runDetails = MutableLiveData<RunsModel>()
    val runDetails: LiveData<RunsModel>
        get() = _runDetails

    var seconds = 0

    fun getAllUserRuns(): Single<List<RunsModel>> {
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        viewModelScope.launch {
            dataSource.getAllRuns(uid)
        }
        return runsRepository.getAllRuns(uid)
    }

    fun getUserRun(uid: String, runId: String): Single<RunsModel> {
        viewModelScope.launch {
            dataSource.getRun(runId)
        }
        return runsRepository.getRun(uid, runId)
    }

    fun setRunDetails(runDetails: RunsModel) {
        _runDetails.postValue(runDetails)
    }
}