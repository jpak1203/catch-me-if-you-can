package com.example.catchmeifyoucan.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.example.catchmeifyoucan.AutoDisposable
import com.example.catchmeifyoucan.R
import dagger.android.support.DaggerAppCompatActivity

abstract class BaseActivity: DaggerAppCompatActivity() {

    companion object {
        private val TAG = BaseActivity::class.java.simpleName
    }

    val autoDisposable = AutoDisposable()

    /**
     * Call before super.onCreate
     */
    private fun bindLifecycle() {
        autoDisposable.bindTo(this.lifecycle)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        bindLifecycle()
        super.onCreate(savedInstanceState)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_navigation_controller).navigateUp()
    }

    fun setToolbar(toolbar: Toolbar, title: String = "") {
        toolbar.apply {
            this.title = title
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

}