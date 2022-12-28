package com.example.catchmeifyoucan.ui.auth

import com.example.catchmeifyoucan.ui.runs.RunsModel
import com.squareup.moshi.Json

data class UserModel(
    @field:Json(name="email") val email: String,
    @field:Json(name="runs") val runs: RunsModel?
)
