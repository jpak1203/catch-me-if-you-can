package com.example.catchmeifyoucan.dagger.modules

import android.app.Application
import androidx.room.Room
import com.example.catchmeifyoucan.dao.AppDatabase
import com.example.catchmeifyoucan.dao.RemindersLocalRepository
import com.example.catchmeifyoucan.dao.RunsDao
import com.example.catchmeifyoucan.dao.RunsDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class DaoModule {
    companion object {

        @Singleton
        @Provides
        fun providesDatabase(app: Application): AppDatabase {
            return Room
                .databaseBuilder(
                    app.applicationContext, AppDatabase::class.java, "catch_me_if_you_can_db"
                )
                .fallbackToDestructiveMigration()
                .build()
        }

        @Provides
        @Singleton
        fun providesDataSource(database: AppDatabase): RunsDataSource {
            return RemindersLocalRepository(database.runsDao())
        }

        @Provides
        @Singleton
        fun provideRunsDao(database: AppDatabase): RunsDao {
            return database.runsDao()
        }
    }
}