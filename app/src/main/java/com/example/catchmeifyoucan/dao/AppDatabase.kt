package com.example.catchmeifyoucan.dao

import androidx.room.*
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "runs")
data class Runs(
    @PrimaryKey val id: Int,
    val userId: Int,
    @ColumnInfo(name = "start_lat") var start_lat: Double = 0.0,
    @ColumnInfo(name = "start_lng") var start_lng: Double = 0.0,
    @ColumnInfo(name = "end_lat") var end_lat: Double = 0.0,
    @ColumnInfo(name = "end_lng") var end_lng: Double = 0.0,
    @ColumnInfo(name ="time") val time: Long
)

@Dao
interface RunsDao {
    @Query("SELECT * FROM runs")
    fun getAll(): List<Runs>

    @Query("SELECT * FROM runs WHERE userId IN (:userId)")
    fun loadRunsOfUser(userId: Int): List<Runs>

    @Query("SELECT * FROM runs WHERE id IN (:id)")
    fun loadRunById(id: Int): Runs

    @Insert
    fun insertAll(vararg runs: Runs)

    @Delete
    fun delete(runs: Runs)
}

@Database( entities = [Runs::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runsDao(): RunsDao
}