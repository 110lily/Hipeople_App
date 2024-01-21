package com.myungwoo.hipeople.firebase

import com.google.firebase.auth.FirebaseAuth

class FBAuth {

    companion object {
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        fun getUid(): String {
            auth = FirebaseAuth.getInstance()
            return auth.currentUser?.uid.toString()
        }
    }
}