package com.example.catchmeifyoucan.dagger.modules

import com.example.catchmeifyoucan.activities.HomeActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    companion object {
        private val TAG = ActivityModule::class.java.simpleName
    }

    @ContributesAndroidInjector
    internal abstract fun contributeHomeActivity(): HomeActivity

}