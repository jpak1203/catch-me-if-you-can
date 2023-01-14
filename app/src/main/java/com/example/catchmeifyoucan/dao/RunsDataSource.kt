package com.example.catchmeifyoucan.dao

import com.example.catchmeifyoucan.ui.runs.RunsModel

interface RunsDataSource {
    suspend fun getAllRuns(uid: String): Result<List<RunsModel>>
    suspend fun saveRun(run: RunsModel)
    suspend fun getRun(id: String): Result<RunsModel>
    suspend fun deleteRun(run: RunsModel)
}