package com.myungwoo.hipeople.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ActivityLikePeopleBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.adapter.LikePeopleAdapter

class LikePeopleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLikePeopleBinding
    private lateinit var dataList: MutableList<UserInfoData>
    private lateinit var adapter: LikePeopleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikePeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataList = mutableListOf()
        val likePeopleRef = Firebase.database.reference.child("likePeople")
        val currentUserUid = Firebase.auth.currentUser!!.uid

        likePeopleRef.child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val likedUserId = data.key.toString()
                        val usersRef = Firebase.database.reference.child("User").child("users")
                        usersRef.child(likedUserId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val userData = snapshot.getValue(UserInfoData::class.java)
                                    if (userData != null) {
                                        dataList.add(userData)
                                        adapter.notifyDataSetChanged()
                                        Log.e("likedUserId", "${dataList}")
                                    }
                                }
                            })
                    }
                }
            })
        adapter = LikePeopleAdapter(this, dataList)
        binding.recyclerView2.adapter = adapter
        binding.recyclerView2.layoutManager = LinearLayoutManager(this)
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, AppMainActivity::class.java))
        finish()
    }
}