package com.example.catchmeifyoucan.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.ActivityMainBinding
import com.example.catchmeifyoucan.login.LoginFragmentViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView.*
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navigationHeader: View
    private val viewModel by viewModels<MainActivityViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (savedInstanceState == null) {
            navController = findNavController(R.id.fragment_navigation_controller)
        }

        subscribe()
        initView()
        setNavigation()
    }

    private fun subscribe() {
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    private fun initView() {
        navigationHeader = binding.navigationView.getHeaderView(0)
    }

    private fun setNavigation() {
        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.fragment_navigation_controller) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.main_navgraph)
        graph.setStartDestination(viewModel.getNavigationStartDestination())
        navHostFragment.navController.graph = graph
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Timber.i(TAG, "navigation to: ${item.itemId}")
        if (item.itemId == navController.currentDestination?.id) {
            return true
        }
        when (item.itemId) {
            R.id.navigation_drawer_home -> {
                navController.navigate(R.id.home_fragment)
            }
            R.id.navigation_drawer_account -> {
                navController.navigate(R.id.account_fragment)
            }
            R.id.navigation_drawer_maps -> {
                navController.navigate(R.id.maps_fragment)
            }
            R.id.navigation_drawer_news -> {
                navController.navigate(R.id.news_fragment)
            }
            R.id.navigation_drawer_logout -> {
                logoutConfirmation()
            }
            else -> {
                Timber.e("destination not found in menu")
            }
        }
        binding.drawerLayout.close()
        return false
    }

    private fun logoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setTitle(getString(R.string.logout))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(R.string.ok) { _, _ -> logout() }
            .setNegativeButton(R.string.cancel) { _, _ -> /* do nothing */ }
            .show()
    }

    private fun logout() {
        AuthUI.getInstance().signOut(applicationContext)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val intent =
                        Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }
}