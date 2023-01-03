package com.example.catchmeifyoucan.ui.runs

import com.firebase.geofire.GeoLocation
import com.squareup.moshi.Json

data class RunsModel(
    @field:Json(name = "time_stamp") var timeStamp: String = "",
    @field:Json(name = "start_lat") var start_lat: Double = 0.0,
    @field:Json(name = "start_lng") var start_lng: Double = 0.0,
    @field:Json(name = "end_lat") var end_lat: Double = 0.0,
    @field:Json(name = "end_lng") var end_lng: Double = 0.0,
    @field:Json(name = "time") var time: Int = 0,
    @field:Json(name = "location_list") var locationList: MutableList<GeoLocation> = mutableListOf(),
    @field:Json(name = "step_count") var stepCount: Int = 0
)
