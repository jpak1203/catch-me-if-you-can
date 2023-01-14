package com.example.catchmeifyoucan.dagger.modules

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.catchmeifyoucan.MyApplication
import com.example.catchmeifyoucan.dao.AppDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
internal class ApplicationModule {

    companion object {
        private val TAG = ApplicationModule::class.java.simpleName
    }

    @Provides
    @Singleton
    fun provideApplication(app: Application): MyApplication = (app as MyApplication)

    @Singleton
    @Provides
    fun providesContext(boxedApplication: MyApplication): Context {
        return boxedApplication.applicationContext
    }
}