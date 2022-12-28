package com.example.catchmeifyoucan.dagger

import android.app.Application
import com.example.catchmeifyoucan.MyApplication
import com.example.catchmeifyoucan.dagger.modules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    ApplicationModule::class,
    ActivityModule::class,
    FragmentModule::class,
    ViewModelModule::class,
    NetworkModule::class,])
interface AppComponent: AndroidInjector<MyApplication> {

    companion object {
        private val TAG = AppComponent::class.java.simpleName
    }

    @Component.Builder
    interface Builder {
        // provide Application instance into dependency injection interface
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

}