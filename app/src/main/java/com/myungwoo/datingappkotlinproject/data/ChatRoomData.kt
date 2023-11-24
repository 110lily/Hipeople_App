package com.myungwoo.datingappkotlinproject.data

// 채팅방 데이터 클래스
data class ChatRoomData(
    val users: Map<String, Boolean>? = HashMap(),
    val users2: Map<String, Boolean>? = HashMap(),
    var messages: Map<String, MessageData>? = HashMap()
) :java.io.Serializable


