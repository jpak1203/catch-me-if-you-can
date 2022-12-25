package com.example.catchmeifyoucan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.catchmeifyoucan.databinding.FragmentStartupBinding

class StartupFragment : Fragment() {

    companion object {
        private val TAG = StartupFragment::class.java.simpleName
    }

    private lateinit var binding: FragmentStartupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStartupBinding.inflate(inflater, container, false)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initView()
        return binding.root
    }

    private fun initView() {
        binding.signupButton.setOnClickListener {
            findNavController().navigate(R.id.signup_fragment)
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.login_fragment)
        }
    }
}