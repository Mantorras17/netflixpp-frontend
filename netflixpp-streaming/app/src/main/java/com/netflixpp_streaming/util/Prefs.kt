package com.netflixpp_streaming.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.netflixpp_streaming.model.User

object Prefs {
    private const val PREFS_NAME = "netflixpp_streaming_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER = "user"
    private const val KEY_MY_LIST = "my_list"

    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getSharedPreferences(context).edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String {
        return getSharedPreferences(context).getString(KEY_TOKEN, "") ?: ""
    }

    fun saveUser(context: Context, user: User) {
        val userJson = Gson().toJson(user)
        getSharedPreferences(context).edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(context: Context): User? {
        val userJson = getSharedPreferences(context).getString(KEY_USER, null)
        return if (userJson != null) {
            Gson().fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun saveMyList(context: Context, videoIds: List<String>) {
        val myListJson = Gson().toJson(videoIds)
        getSharedPreferences(context).edit().putString(KEY_MY_LIST, myListJson).apply()
    }

    fun getMyList(context: Context): List<String> {
        val myListJson = getSharedPreferences(context).getString(KEY_MY_LIST, null)
        return if (myListJson != null) {
            Gson().fromJson(myListJson, Array<String>::class.java).toList()
        } else {
            emptyList()
        }
    }

    fun clearUserData(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}