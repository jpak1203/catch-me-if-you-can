package com.example.catchmeifyoucan.ui.runs

import com.squareup.moshi.Json

data class RunsModel(
    @field:Json(name="id") val id: Int,
    @field:Json(name="start_lat") var start_lat: Double = 0.0,
    @field:Json(name = "start_lng") var start_lng: Double = 0.0,
    @field:Json(name = "end_lat") var end_lat: Double = 0.0,
    @field:Json(name = "end_lng") var end_lng: Double = 0.0,
    @field:Json(name ="time") val time: Double
)
