package com.example.catchmeifyoucan.utils

import androidx.annotation.CallSuper
import androidx.fragment.app.FragmentActivity
import com.example.catchmeifyoucan.rxjava.AbstractSingleObserver
import com.example.catchmeifyoucan.ui.LoadingAnimationDialog
import io.reactivex.rxjava3.disposables.Disposable

open class LoadingDialogSingle<T>(private val context: FragmentActivity): AbstractSingleObserver<T>() {

    companion object {
        private val TAG = LoadingDialogSingle::class.java.simpleName
    }

    private lateinit var dialog: LoadingAnimationDialog

    @CallSuper
    override fun onSubscribe(d: Disposable) {
        dialog = LoadingAnimationDialog()
        dialog.show(context.supportFragmentManager, TAG)
    }

    @CallSuper
    override fun onError(e: Throwable) {
        super.onError(e)
        dialog.dismiss()
    }

    @CallSuper
    override fun onSuccess(t: T)  {
        super.onSuccess(t)
        dialog.dismiss()
    }

}