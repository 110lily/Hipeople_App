package com.myungwoo.hipeople.data

data class MessageData(
    var senderUid: String = "",
    var sendedDate: String = "",
    var content: String = "",
    var confirmed: Boolean = false
) : java.io.Serializable

