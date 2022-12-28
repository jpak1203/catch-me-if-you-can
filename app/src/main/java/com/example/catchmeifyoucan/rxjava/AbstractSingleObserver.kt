package com.example.catchmeifyoucan.rxjava

import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

abstract class AbstractSingleObserver<T>: SingleObserver<T> {

    companion object {
        private val TAG = AbstractSingleObserver::class.java.simpleName
    }

    override fun onSubscribe(d: Disposable) { /* unused */ }

    override fun onSuccess(t: T) { /* unused */ }

    override fun onError(e: Throwable) { /* unused */ }

}