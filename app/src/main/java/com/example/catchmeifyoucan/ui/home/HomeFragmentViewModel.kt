package com.example.catchmeifyoucan.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catchmeifyoucan.dao.RunsDataSource
import com.example.catchmeifyoucan.ui.runs.RunsModel
import com.example.catchmeifyoucan.ui.runs.RunsRepository
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(private val runsRepository: RunsRepository,
                                                private val dataSource: RunsDataSource): ViewModel() {

    private val _runData = MutableLiveData(RunsModel(id = UUID.randomUUID().toString(),
        FirebaseAuth.getInstance().currentUser!!.uid))
    val runData: LiveData<RunsModel>
        get() = _runData

    var seconds = 0
    var stepCount = 0

    fun setTimestamp(timeStamp: String) {
        _runData.value!!.timeStamp = timeStamp
        _runData.postValue(runData.value)
    }

    fun setStartLatLng(latLng: LatLng) {
        _runData.value!!.start_lat = latLng.latitude
        _runData.value!!.start_lng = latLng.longitude
        _runData.postValue(runData.value)
    }

    fun setEndLatLng(latLng: LatLng) {
        _runData.value!!.end_lat = latLng.latitude
        _runData.value!!.end_lng = latLng.longitude
        _runData.postValue(runData.value)
    }

    fun setRunTime() {
        _runData.value!!.time = seconds
        _runData.postValue(runData.value)
    }

    fun setStepCount() {
        _runData.value!!.stepCount = stepCount
    }

    fun saveRun() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: ""
        runData.value?.let {
            viewModelScope.launch {
                dataSource.saveRun(it)
            }
            runsRepository.saveRun(uid, it.id, it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
    }



}