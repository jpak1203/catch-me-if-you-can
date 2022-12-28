package com.example.catchmeifyoucan.ui.home

import androidx.lifecycle.ViewModel
import com.example.catchmeifyoucan.ui.auth.UserRepository
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(private val userRepository: UserRepository): ViewModel() {

}