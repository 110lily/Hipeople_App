package com.example.datingappkotlinproject.chat.fcm

import com.example.datingappkotlinproject.BuildConfig

class Repo {
    companion object{

        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = BuildConfig.server_key
        const val CONTENT_TYPE = "application/json"

    }
}