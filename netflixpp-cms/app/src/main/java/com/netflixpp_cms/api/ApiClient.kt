package com.netflixpp_cms.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Update with your server IP - use 10.0.2.2 for Android emulator to connect to localhost
    // This is Hugo's local IP path
    private const val BASE_URL = "http://10.149.118.98:8080/api/"

    private var retrofit: Retrofit? = null

    fun getApiService(context: Context): ApiService {
        if (retrofit == null) {
            retrofit = createRetrofit(context)
        }
        return retrofit!!.create(ApiService::class.java)
    }

    private fun createRetrofit(context: Context): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = AuthInterceptor(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Para compatibilidade com código existente
    val apiService: ApiService? = null
}