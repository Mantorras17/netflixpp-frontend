package com.netflixpp_cms.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import com.netflixpp_cms.util.Prefs

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Obter o token salvo
        val token = Prefs.getToken(context)

        // Se tiver token, adicionar ao header
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