package com.example.catchmeifyoucan.dagger.interceptors

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.*
import com.example.catchmeifyoucan.BuildConfig
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.util.*

class ApiHeaders(private val context: Context): Interceptor {

    companion object {
        private val TAG = ApiHeaders::class.java.simpleName

        private const val API_VERSION = "2.6"

        const val HEADER_PARAM_API_VERSION = "v"
        const val HEADER_PARAM_DEVICE_ID = "deviceId"
        const val HEADER_PARAM_APP_VERSION = "appVersion"
        const val HEADER_PARAM_DEVICE_LOCALE = "accept-language"
    }

    @SuppressLint("HardwareIds")
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder: HttpUrl.Builder = original.url.newBuilder()

        builder.addEncodedQueryParameter(HEADER_PARAM_API_VERSION, API_VERSION)
        Timber.d("adding encoded query parameter: $HEADER_PARAM_API_VERSION=$API_VERSION")
        val deviceId = Secure.getString(
            context.contentResolver,
            Secure.ANDROID_ID
        )
        Timber.d("adding encoded query parameter: $HEADER_PARAM_DEVICE_ID=$deviceId")
        if (deviceId != null && deviceId.isNotEmpty()) {
            builder.addEncodedQueryParameter(HEADER_PARAM_DEVICE_ID, deviceId)
        }

        val versionName = BuildConfig.VERSION_NAME
        builder.addEncodedQueryParameter(HEADER_PARAM_APP_VERSION, versionName)
        Timber.d("adding encoded query parameter: $HEADER_PARAM_APP_VERSION=$versionName")

        val localizationTag = Locale.getDefault().toLanguageTag()
        builder.addEncodedQueryParameter(HEADER_PARAM_DEVICE_LOCALE, localizationTag)
        Timber.d("adding encoded query parameter: $HEADER_PARAM_DEVICE_LOCALE=$localizationTag")

        val url: HttpUrl = builder.build()
        Timber.d("HttpUrl: $url")

        // Request customization: add request headers
        val requestBuilder: Request.Builder = original.newBuilder().url(url)

        val request: Request = requestBuilder.build()
        Timber.d("$request")
        return chain.proceed(request)
    }

}