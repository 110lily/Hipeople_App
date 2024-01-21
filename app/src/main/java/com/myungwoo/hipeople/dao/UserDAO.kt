package com.myungwoo.hipeople.dao

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query

class UserDAO {
    private var databaseReference: DatabaseReference? = null

    init {
        val db = FirebaseDatabase.getInstance()
        databaseReference = db.getReference("posting")
    }

    fun userSelectComment(postingPath: String): Query {
        return databaseReference!!.child(postingPath).child("comment")
    }
}