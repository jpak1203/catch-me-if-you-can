package com.example.catchmeifyoucan.dao

import androidx.room.*
import com.example.catchmeifyoucan.ui.runs.RunsModel

@Dao
interface RunsDao {
    @Query("SELECT * FROM runs WHERE uid IN (:uid)")
    fun getAll(uid: String): List<RunsModel>

    @Query("SELECT * FROM runs WHERE id IN (:id)")
    fun loadRunById(id: String): RunsModel

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRun(vararg run: RunsModel)

    @Delete
    fun delete(run: RunsModel)
}

@Database( entities = [RunsModel::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runsDao(): RunsDao
}