package com.example.catchmeifyoucan.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.ui.runs.RunsModel
import com.example.catchmeifyoucan.ui.runs.RunsRepository
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(private val runsRepository: RunsRepository): ViewModel() {

    private val _runData = MutableLiveData(RunsModel())
    val runData: LiveData<RunsModel>
        get() = _runData

    private val _locationList = MutableLiveData(mutableListOf<GeoLocation>())
    val locationList: LiveData<MutableList<GeoLocation>>
        get() = _locationList

    var seconds = 0
    var initialStepCount = -1

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

    fun setRunLocationList() {
        _runData.value!!.locationList = locationList.value!!
    }

    fun saveRun() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: ""
        val runId: String = UUID.randomUUID().toString()
        runData.value?.let {
            runsRepository.saveRun(uid, runId, it)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe()
        }
    }



}