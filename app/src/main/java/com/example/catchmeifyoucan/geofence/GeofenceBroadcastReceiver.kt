package com.example.catchmeifyoucan.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
        private var geoFencePref: SharedPreferences? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        geoFencePref = context?.getSharedPreferences("TriggerdExitedId",Context.MODE_PRIVATE)
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent!!.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Timber.e(errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            storeGeofenceTransitionDetails(triggeringGeofences!![0])
        } else {
            Timber.e("Invalid type transition $geofenceTransition")
        }
    }

    private fun storeGeofenceTransitionDetails(triggeredGeofence: Geofence) {
        geoFencePref?.edit()?.putString("geoFenceId", triggeredGeofence.requestId)?.apply()
    }
}