package com.example.catchmeifyoucan.dagger.modules

import com.example.catchmeifyoucan.ui.account.AccountFragment
import com.example.catchmeifyoucan.ui.account.ChangePasswordFragment
import com.example.catchmeifyoucan.ui.account.DeleteAccountFragment
import com.example.catchmeifyoucan.ui.account.UpdateAccountNameFragment
import com.example.catchmeifyoucan.ui.auth.ForgetPasswordFragment
import com.example.catchmeifyoucan.ui.auth.LoginFragment
import com.example.catchmeifyoucan.ui.auth.SignupFragment
import com.example.catchmeifyoucan.ui.challenges.ChallengesFragment
import com.example.catchmeifyoucan.ui.friends.FriendsFragment
import com.example.catchmeifyoucan.ui.home.HomeFragment
import com.example.catchmeifyoucan.ui.news.NewsFragment
import com.example.catchmeifyoucan.ui.runs.RunDetailsFragment
import com.example.catchmeifyoucan.ui.runs.RunsFragment
import com.example.catchmeifyoucan.utils.PermissionsUtil
import com.example.catchmeifyoucan.utils.ValidatorUtil
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    companion object {
        private val TAG = FragmentModule::class.java.simpleName
    }

    @ContributesAndroidInjector
    internal abstract fun contributeValidatorUtil(): ValidatorUtil

    @ContributesAndroidInjector
    internal abstract fun contributePermissionsUtil(): PermissionsUtil

    @ContributesAndroidInjector
    internal abstract fun contributeHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    internal abstract fun contributeAccountFragment(): AccountFragment

    @ContributesAndroidInjector
    internal abstract fun contributeForgetPasswordFragment(): ForgetPasswordFragment

    @ContributesAndroidInjector
    internal abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    internal abstract fun contributeSignupFragment(): SignupFragment

    @ContributesAndroidInjector
    internal abstract fun contributeChallengesFragment(): ChallengesFragment

    @ContributesAndroidInjector
    internal abstract fun contributeFriendsFragment(): FriendsFragment

    @ContributesAndroidInjector
    internal abstract fun contributeNewsFragment(): NewsFragment

    @ContributesAndroidInjector
    internal abstract fun contributeRunsFragment(): RunsFragment

    @ContributesAndroidInjector
    internal abstract fun contributeRunDetailsFragment(): RunDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun contributeUpdateAccountNameFragment(): UpdateAccountNameFragment

    @ContributesAndroidInjector
    internal abstract fun contributeChangePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector
    internal abstract fun contributeDeleteAccountFragment(): DeleteAccountFragment
}