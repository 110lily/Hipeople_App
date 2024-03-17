package com.myungwoo.hipeople.fcm

// 푸시 알림을 보내기 위한 전체 데이터 구조
class PushNotification(
    val data: NotiModel,
    val to: String
)