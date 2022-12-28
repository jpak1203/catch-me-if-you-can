package com.example.catchmeifyoucan.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.ui.home.HomeFragment
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import timber.log.Timber

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
        private const val NOTIFICATION_ID = 33
        private const val CHANNEL_ID = "GeofenceChannel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)!!
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Timber.e(TAG, errorMessage)
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            val notificationManager = ContextCompat.getSystemService(
                context, NotificationManager::class.java
            ) as NotificationManager

            notificationManager.sendGeofenceEnteredNotification(context)
        } else {
            Timber.e(TAG, "Invalid type transition $geofenceTransition")
        }
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, "Channel1", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
    // extension function
    fun NotificationManager.sendGeofenceEnteredNotification(context: Context) {

        // Opening the notification
        val contentIntent = Intent(context, HomeFragment::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Building the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("You have entered a geofence area")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .build()

        this.notify(NOTIFICATION_ID, builder)
    }
}