package com.myungwoo.hipeople.data

data class UserInfoData(
    val uid: String? = "",
    val id: String? = "",
    val pw: String? = "",
    val gender: String? = "",
    val purpose: String? = "",
    val birth: String? = "",
    val height: String? = "",
    val weight: String? = "",
    val address: String? = "",
    val edu: String? = "",
    val nickName: String? = "",
    val picture: String? = "",
    val like: Int? = 0,
    val communityNickname: String? = "",
    val token: String? = ""
) : java.io.Serializable
