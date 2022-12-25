package com.example.catchmeifyoucan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.catchmeifyoucan.BaseFragment
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.activities.HomeActivity
import com.example.catchmeifyoucan.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<HomeFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }

        initView()
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as HomeActivity).unlockNavigationDrawer()
    }

    private fun initView() {
        (requireActivity() as HomeActivity).hideToolbar()
        (requireActivity() as HomeActivity).setSupportActionBar(binding.toolbar)
        val drawerLayout = (requireActivity() as HomeActivity).findViewById<DrawerLayout>(R.id.drawer_layout)
        val drawerToggle =
            ActionBarDrawerToggle(requireActivity(),
                drawerLayout, binding.toolbar,
                R.string.content_nav_open, R.string.content_nav_closed)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        (requireActivity() as HomeActivity).title = getString(R.string.app_name)
        (requireActivity() as HomeActivity).setUserEmail()
    }
}