package com.example.catchmeifyoucan

import android.app.Application
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.example.catchmeifyoucan.dagger.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import timber.log.Timber
import timber.log.Timber.Forest.plant
import javax.inject.Inject

class MyApplication : Application(), HasAndroidInjector {

    companion object {
        private val TAG = MyApplication::class.java.simpleName
    }

    @Inject
    lateinit var androidInjector : DispatchingAndroidInjector<Any>

    override fun onCreate() {
        super.onCreate()
        DaggerAppComponent.builder().application(this).build().inject(this)
        if (BuildConfig.DEBUG) {
            val logger = Timber.DebugTree()
            plant(logger)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return androidInjector
    }

}

/**
 * Controls Glide’s memory and disk cache usage. Must be exactly one AppGlideModule implementation.
 * Optionally, add one or more LibraryGlideModule implementations.
 * Refer to https://bumptech.github.io/glide/doc/configuration.html#application-options
 */
@GlideModule
class ImageModule: AppGlideModule()