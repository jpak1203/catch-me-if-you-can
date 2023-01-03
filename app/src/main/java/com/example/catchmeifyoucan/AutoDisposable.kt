package com.example.catchmeifyoucan

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class AutoDisposable {

    companion object {
        private val TAG = AutoDisposable::class.java.simpleName
    }

    private lateinit var compositeDisposable: CompositeDisposable

    fun bindTo(lifecycle: Lifecycle) {
        lifecycle.addObserver(lifecycleEventObserver)
        compositeDisposable = CompositeDisposable()
    }

    fun add(disposable: Disposable) {
        if (::compositeDisposable.isInitialized) {
            compositeDisposable.add(disposable)
        } else {
            throw NotImplementedError("Must bind AutoDisposable to a fragment or activity lifecycle")
        }
    }

    private val lifecycleEventObserver = LifecycleEventObserver { source, event ->
        if ( event == Lifecycle.Event.ON_PAUSE ) {
            compositeDisposable.clear()
        }
    }

}