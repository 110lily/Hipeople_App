package com.myungwoo.hipeople.fcm

import com.myungwoo.hipeople.fcm.Repo.Companion.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Retrofit 인스턴스를 생성하고 NotiAPI 인터페이스의 구현체를 제공
class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val api = retrofit.create(NotiAPI::class.java)
    }
}