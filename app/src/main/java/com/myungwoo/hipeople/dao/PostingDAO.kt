package com.myungwoo.hipeople.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.myungwoo.hipeople.data.AnonymousPostingData

class PostingDAO {
    var databaseReference: DatabaseReference? = null
    var storage: FirebaseStorage? = null

    init {
        val db = FirebaseDatabase.getInstance()
        databaseReference = db.getReference("posting")
        storage = Firebase.storage
    }

    fun pictureInsert(anonymousPostingData: AnonymousPostingData): Task<Void> {
        return databaseReference!!.push().setValue(anonymousPostingData)
    }

    fun pictureSelect(): Query? {
        return databaseReference
    }
}