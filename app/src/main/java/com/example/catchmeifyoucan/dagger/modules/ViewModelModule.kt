package com.example.catchmeifyoucan.dagger.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.catchmeifyoucan.activities.HomeActivityViewModel
import com.example.catchmeifyoucan.dagger.factory.ViewModelFactory
import com.example.catchmeifyoucan.dagger.factory.ViewModelKey
import com.example.catchmeifyoucan.ui.account.AccountFragmentViewModel
import com.example.catchmeifyoucan.ui.auth.LoginFragmentViewModel
import com.example.catchmeifyoucan.ui.auth.SignupFragmentViewModel
import com.example.catchmeifyoucan.ui.home.HomeFragmentViewModel
import com.example.catchmeifyoucan.ui.runs.RunsFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    companion object {
        private val TAG = ViewModelModule::class.java.simpleName
    }

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(HomeActivityViewModel::class)
    internal abstract fun homeActivityViewModel(viewModel: HomeActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginFragmentViewModel::class)
    internal abstract fun loginFragmentViewModel(viewModel: LoginFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignupFragmentViewModel::class)
    internal abstract fun signupFragmentViewModel(viewModel: SignupFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeFragmentViewModel::class)
    internal abstract fun homeFragmentViewModel(viewModel: HomeFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RunsFragmentViewModel::class)
    internal abstract fun runsFragmentViewModel(viewModel: RunsFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AccountFragmentViewModel::class)
    internal abstract fun accountFragmentViewModel(viewModel: AccountFragmentViewModel): ViewModel

}