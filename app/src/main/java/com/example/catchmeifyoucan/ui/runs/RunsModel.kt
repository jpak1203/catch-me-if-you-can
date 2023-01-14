package com.example.catchmeifyoucan.ui.runs

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.firebase.geofire.GeoLocation
import com.squareup.moshi.Json

@Entity(tableName = "runs")
data class RunsModel(
    @PrimaryKey @field:Json(name = "id") var id: String,
    @field:Json(name = "uid") var uid: String,
    @field:Json(name = "time_stamp") var timeStamp: String = "",
    @field:Json(name = "start_lat") var start_lat: Double = 0.0,
    @field:Json(name = "start_lng") var start_lng: Double = 0.0,
    @field:Json(name = "end_lat") var end_lat: Double = 0.0,
    @field:Json(name = "end_lng") var end_lng: Double = 0.0,
    @field:Json(name = "time") var time: Int = 0,
    @field:Json(name = "step_count") var stepCount: Int = 0
)
