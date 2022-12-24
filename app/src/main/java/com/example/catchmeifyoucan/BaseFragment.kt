package com.example.catchmeifyoucan

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class BaseFragment: Fragment(), MenuProvider {

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.toolbar_challenges -> {
                findNavController().navigate(R.id.challenges_fragment)
            }
            R.id.toolbar_newsfeed -> {
                findNavController().navigate(R.id.news_fragment)
            }
            R.id.toolbar_friends -> {
                findNavController().navigate(R.id.friends_fragment)
            }
        }
        return false
    }
}