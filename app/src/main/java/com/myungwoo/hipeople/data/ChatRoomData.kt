package com.myungwoo.hipeople.data

data class ChatRoomData(
    val users: Map<String, Boolean>? = HashMap(),
    val users2: Map<String, Boolean>? = HashMap(),
    var messages: Map<String, MessageData>? = HashMap()
) : java.io.Serializable

