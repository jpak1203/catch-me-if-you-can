package com.example.catchmeifyoucan.activities

import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import com.example.catchmeifyoucan.R
import dagger.android.support.DaggerAppCompatActivity

abstract class MainActivity: DaggerAppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
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