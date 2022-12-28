package com.example.catchmeifyoucan.dagger.interceptors

import android.content.Context
import android.content.Intent
import com.example.catchmeifyoucan.BuildConfig
import com.example.catchmeifyoucan.R
import com.example.catchmeifyoucan.utils.NetworkUtil.acceptedStatusCode
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class DebugInterceptor(
    private val context: Context,
) : Interceptor {
    companion object {
        private val TAG = DebugInterceptor::class.java.simpleName

        const val ARG_DEBUG_RESPONSE = "arg_debug_response"
        const val ARG_REQUEST = "arg_request"
        const val ARG_TITLE = "arg_title"
        const val ARG_MESSAGE = "arg_message"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        // throw exception for non-2XX. HTTP 201, 204, etc are generally OK.
        if (!acceptedStatusCode(response.code)) {
            val responseAsString = response.body?.use { it.string() } ?: ""

            val intent = Intent(ARG_DEBUG_RESPONSE)
            intent.putExtra(ARG_REQUEST, response.request.url.toString().removePrefix(BuildConfig.BASE_URL))
            intent.putExtra(ARG_TITLE, String.format(context.getString(R.string.debug_utils_status_code_formatted), response.code))
            intent.putExtra(ARG_MESSAGE, responseAsString)
            context.sendBroadcast(intent)

            val body = responseAsString.toResponseBody()
            return response.newBuilder().body(body).build()
        }
        return response
    }

}