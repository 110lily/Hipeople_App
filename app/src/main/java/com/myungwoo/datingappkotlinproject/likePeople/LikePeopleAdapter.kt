package com.myungwoo.datingappkotlinproject.likePeople

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myungwoo.datingappkotlinproject.R
import com.myungwoo.datingappkotlinproject.UserInfoData
import com.myungwoo.datingappkotlinproject.chat.ChatRoom
import com.myungwoo.datingappkotlinproject.chat.ChatRoomActivity
import com.myungwoo.datingappkotlinproject.community.CommentAdapter
import com.myungwoo.datingappkotlinproject.databinding.ItemPhotoBinding
import com.myungwoo.datingappkotlinproject.databinding.LikepeopleItemBinding
import com.myungwoo.datingappkotlinproject.databinding.ListChatroomItemBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class LikePeopleAdapter( val context: Context, val items: MutableList<UserInfoData>) :
    RecyclerView.Adapter<LikePeopleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LikepeopleItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val binding = holder.binding
        val itemsData = items[position]
        binding.tvName.text = items[position].nickName
        // uid값에 따른 사진 로드
        val pictureRef = Firebase.storage.reference.child("${itemsData.uid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                // Glide 를 통하여 imageView에 사진 로드
                Glide.with(context).load(it.result).into(binding.ivProfile)
            }
        }

        //채팅이미지 클릭시 chatRoom으로 이동
        binding.chatBtn.setOnClickListener {
            addChatRoom(position)

        }
    }

    inner class ViewHolder(val binding: LikepeopleItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    fun addChatRoom(position: Int) {    // 채팅방 추가
        val opponent = items[position]      // 채팅할 상대방 정보
        val currentUserUid = Firebase.auth.currentUser!!.uid //내정보
        val database =
            FirebaseDatabase.getInstance().getReference("ChatRoom") // 넣을 database reference 세팅
        val chatRoom = ChatRoom(        // 추가할 채팅방 정보 세팅
            // 현재 사용자 uid, 상대방 uid, 현재 사용자 + 상대방 uid, 상대방 uid + 현재 사용자 uid
            mapOf(Firebase.auth.currentUser!!.uid!! to (true), opponent.uid!! to (true)),
            mapOf(
                "${currentUserUid}${opponent.uid}"!! to (true),
                "${opponent.uid}${currentUserUid}"!! to (true)
            ),
            null
        )

        database.child("chatRooms")
            .orderByChild("users2/${opponent.uid}${currentUserUid}")
            .equalTo(true) //상대방 Uid가 포함된 채팅방이 있는 지 확인
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) { //채팅방이 없는 경우
                        // 채팅방 새로 생성 후 이동
                        database.child("chatRooms").push().setValue(chatRoom)
                            .addOnSuccessListener {// 채팅방 새로 생성 후 이동

                                goToChatRoom(chatRoom, opponent, "")

                            }
                    } else {
//                        context.startActivity(Intent(context, MainActivity::class.java))
                        goToChatRoom(chatRoom, opponent, "")                    //해당 채팅방으로 이동
                    }

                }
            })
    }

    // 채팅방 이동 함수
    fun goToChatRoom(
        chatRoom: ChatRoom,
        opponentUid: UserInfoData,
        chatRoomKey: String
    ) {       //채팅방으로 이동
        var intent = Intent(context, ChatRoomActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)       //채팅방 정보
        intent.putExtra("Opponent", opponentUid)    //상대방 정보
        intent.putExtra("ChatRoomKey", "") //채팅방 키
        context.startActivity(intent)
    }


}