package com.example.catchmeifyoucan.dao

import androidx.room.*

@Entity(tableName = "runs")
data class Runs(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "start_lat") var start_lat: Double = 0.0,
    @ColumnInfo(name = "start_lng") var start_lng: Double = 0.0,
    @ColumnInfo(name = "end_lat") var end_lat: Double = 0.0,
    @ColumnInfo(name = "end_lng") var end_lng: Double = 0.0,
    @ColumnInfo(name ="time") val time: Double
)

@Entity(tableName = "users")
data class Users(
    @PrimaryKey val uid: String,
    @ColumnInfo(name = "email") val email: String,
)

@Dao
interface RunsDao {
    @Query("SELECT * FROM runs")
    fun getAll(): List<Runs>

    @Query("SELECT * FROM runs WHERE id IN (:id)")
    fun loadRunById(id: Int): Runs

    @Insert
    fun insertAll(vararg runs: Runs)

    @Delete
    fun delete(runs: Runs)
}

@Dao
interface UsersDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<Users>

    @Query("SELECT * FROM users WHERE uid IN (:uid)")
    fun loadUser(uid: String): Users

    @Insert
    fun insertAll(vararg users: Users)

    @Delete
    fun delete(users: Users)
}

@Database( entities = [Runs::class, Users::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun runsDao(): RunsDao
    abstract fun usersDao(): UsersDao
}