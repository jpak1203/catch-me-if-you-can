package com.example.catchmeifyoucan.utils

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {

    private const val GLOBAL = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(GLOBAL)

    fun increment(){
        countingIdlingResource.increment()
    }

    fun decrement(){
        if (!countingIdlingResource.isIdleNow){
            countingIdlingResource.decrement()
        }
    }

    inline fun <T> wrapEspressoIdlingResource(function: () -> T) : T{
        EspressoIdlingResource.increment()

        return try {
            function()
        }
        finally {
            EspressoIdlingResource.decrement()
        }
    }
}