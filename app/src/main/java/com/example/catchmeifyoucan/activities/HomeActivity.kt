package com.example.catchmeifyoucan.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.databinding.ActivityHomeBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import timber.log.Timber
import timber.log.Timber.Forest.plant


class HomeActivity : MainActivity(), OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var navigationHeader: View
    private val viewModel by viewModels<HomeActivityViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        if (savedInstanceState == null) {
            navController = findNavController(R.id.fragment_navigation_controller)
        }

        subscribe()
        initView()
        setNavigation()
    }

    private fun subscribe() {
        binding.navigationView.setNavigationItemSelectedListener(this)
        viewModel.email.observe(this) {
            val headerEmailTextView = binding.navigationView.getHeaderView(0)
                .findViewById<TextView>(R.id.navigation_drawer_email)
            if (it.isNotEmpty()) {
                headerEmailTextView.text = it
            } else {
                headerEmailTextView.text = ""
            }
        }
    }

    private fun initView() {
        navigationHeader = binding.navigationView.getHeaderView(0)
        navigationHeader.setOnClickListener {
            navController.navigate(R.id.account_fragment)
            binding.drawerLayout.close()
        }
    }

    private fun setNavigation() {
        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.fragment_navigation_controller) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.main_navgraph)
        graph.setStartDestination(viewModel.getNavigationStartDestination())
        navHostFragment.navController.graph = graph
    }

    fun setUserEmail() {
        viewModel.setUserEmail()
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
            R.id.navigation_drawer_runs -> {
                navController.navigate(R.id.run_history_fragment)
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
                        Intent(applicationContext, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    fun showToolbar(title: String) {
        setToolbar(binding.toolbar, title)
        binding.toolbar.visibility = View.VISIBLE
    }

    fun hideToolbar() {
        binding.toolbar.visibility = View.GONE
    }

    fun setNavigationIcon(resId: Int? = null) {
        this.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(resId ?: R.drawable.ic_arrow_back)
        }
    }

    fun lockNavigationDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    fun unlockNavigationDrawer() {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }
}