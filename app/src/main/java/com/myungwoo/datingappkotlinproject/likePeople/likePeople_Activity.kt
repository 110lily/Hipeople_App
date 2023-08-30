package com.myungwoo.datingappkotlinproject.likePeople

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.myungwoo.datingappkotlinproject.ActivityForMain.AppMainActivity
import com.myungwoo.datingappkotlinproject.UserInfoData
import com.myungwoo.datingappkotlinproject.databinding.ActivityLikePeopleBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class likePeople_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLikePeopleBinding
    var currentUserUid = Firebase.auth.currentUser!!.uid
    lateinit var dataList: MutableList<UserInfoData>
    lateinit var adapter: LikePeopleAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikePeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataList = mutableListOf()
        Log.e("likePeople", currentUserUid)

        // likePeople 노드에 접근합니다.
        val likePeopleRef = Firebase.database.reference.child("likePeople")

        // 현재 사용자의 uid를 가져옵니다.
        val currentUserUid = Firebase.auth.currentUser!!.uid

        // likePeople 노드에서 현재 사용자의 하위 노드를 찾습니다.
        likePeopleRef.child(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        // 각 하위 노드에서 좋아요를 받은 사용자의 uid를 가져와서 처리합니다.
                        val likedUserId = data.key.toString()
                        Log.e("likedUserId", likedUserId)

                        // users 노드에서 해당 사용자의 정보를 가져옵니다.
                        val usersRef = Firebase.database.reference.child("User").child("users")
                        usersRef.child(likedUserId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    // 사용자 정보를 가져와서 dataList에 추가합니다.
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
        // 리사이클러뷰에 댓글 어댑터 연결
        adapter = LikePeopleAdapter(this, dataList)
        binding.recyclerView2.adapter = adapter
        binding.recyclerView2.layoutManager = LinearLayoutManager(this)
        adapter.notifyDataSetChanged()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, com.myungwoo.datingappkotlinproject.ActivityForMain.AppMainActivity::class.java))
        finish()
    }

}