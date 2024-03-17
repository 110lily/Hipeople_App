package com.myungwoo.hipeople.fcm

import com.myungwoo.hipeople.BuildConfig

// FCM 관련 설정값을 보관하는 객체
class Repo {
    companion object {
        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = BuildConfig.server_key
        const val CONTENT_TYPE = "application/json"
    }
}