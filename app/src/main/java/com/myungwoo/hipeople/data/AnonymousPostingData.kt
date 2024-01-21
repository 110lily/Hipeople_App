package com.myungwoo.hipeople.data

import java.io.Serializable

data class AnonymousPostingData(
    var key: String = "",
    var writer: String = "",
    var nickName: String = "",
    var title: String = "",
    var content: String = "",
    var tvLike: Int = 0,
    var tvComment: Int = 0,
    var tvHits: Int = 0,
    var tvDate: String = "",
) : Serializable