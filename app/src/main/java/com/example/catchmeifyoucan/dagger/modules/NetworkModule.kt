package com.example.catchmeifyoucan.dagger.modules

import android.app.Application
import com.example.catchmeifyoucan.BuildConfig
import com.example.catchmeifyoucan.dagger.interceptors.ApiHeaders
import com.example.catchmeifyoucan.dagger.interceptors.DebugInterceptor
import com.example.catchmeifyoucan.dagger.interceptors.UserAgentInterceptor
import com.example.catchmeifyoucan.network.ApiService
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
internal class NetworkModule {

    companion object {
        private val TAG = NetworkModule::class.java.simpleName

        const val NETWORK_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val MS_CONN_TIMEOUT = 45000L
        const val MS_READ_TIMEOUT = 45000L
    }

    @Singleton
    @Provides
    fun provideInterceptors(): ArrayList<Interceptor> {
        val interceptors = arrayListOf<Interceptor>()
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        interceptors.add(loggingInterceptor)
        return interceptors
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(context: Application,
                            interceptors: ArrayList<Interceptor>
    ): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .followRedirects(false)
            .protocols(listOf(Protocol.HTTP_1_1))
            .connectTimeout(MS_CONN_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(MS_READ_TIMEOUT, TimeUnit.MILLISECONDS)

        clientBuilder.interceptors().apply {
            if (BuildConfig.DEBUG) {
                add(DebugInterceptor(context))
            }
            addAll(interceptors)
            add(ApiHeaders(context))
            add(UserAgentInterceptor())
        }
        return clientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

}