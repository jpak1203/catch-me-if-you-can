package com.example.catchmeifyoucan.ui.runs

import com.google.android.gms.maps.model.LatLng

data class RunsModel(
    val start: LatLng,
    val end: LatLng,
    val time: Float
)
