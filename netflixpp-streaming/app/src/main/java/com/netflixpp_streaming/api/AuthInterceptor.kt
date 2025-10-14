package com.netflixpp_streaming.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import com.netflixpp_streaming.util.Prefs

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = Prefs.getToken(context)

        return if (token.isNotEmpty()) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}