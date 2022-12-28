package com.example.catchmeifyoucan.utils

import java.net.HttpURLConnection

object NetworkUtil {

    fun acceptedStatusCode(statusCode: Int): Boolean {
        return statusCode == HttpURLConnection.HTTP_OK
                || statusCode == HttpURLConnection.HTTP_CREATED
                || statusCode == HttpURLConnection.HTTP_ACCEPTED
    }
}