package com.example.catchmeifyoucan.ui.runs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.ui.BaseFragment

class RunsFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_runs, container, false)
    }

}