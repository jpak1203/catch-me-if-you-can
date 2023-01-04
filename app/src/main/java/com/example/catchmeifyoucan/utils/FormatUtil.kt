package com.example.catchmeifyoucan.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object FormatUtil {
    @SuppressLint("SimpleDateFormat")
    fun getDate(ts: String): String {
        val sdf = SimpleDateFormat("MMM dd @ hh:mm a")
        val date = Date(ts.toLong())
        return sdf.format(date)
    }

    fun getRunTime(seconds: Int): String {
        val hours: Int = seconds / 3600
        val minutes: Int = seconds % 3600 / 60
        val secs: Int = seconds % 60

        return java.lang.String.format(
            Locale.getDefault(),
            "%d:%02d:%02d", hours,
            minutes, secs
        )
    }
}