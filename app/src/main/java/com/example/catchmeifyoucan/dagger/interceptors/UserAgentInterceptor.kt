package com.example.catchmeifyoucan.dagger.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.userAgent

open class UserAgentInterceptor(): Interceptor {

    companion object {
        private val TAG = UserAgentInterceptor::class.java.simpleName
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val setUserAgent = userAgent
        val requestWithUserAgent = originRequest.newBuilder()
            .header("User-Agent", setUserAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }

}