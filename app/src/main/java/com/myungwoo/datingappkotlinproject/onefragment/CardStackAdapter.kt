package com.myungwoo.datingappkotlinproject.onefragment

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myungwoo.datingappkotlinproject.Login.FBAuth
import com.myungwoo.datingappkotlinproject.R
import com.myungwoo.datingappkotlinproject.UserInfoData
import com.myungwoo.datingappkotlinproject.chat.ChatRoom
import com.myungwoo.datingappkotlinproject.chat.ChatRoomActivity
import com.myungwoo.datingappkotlinproject.databinding.ItemPhotoBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class CardStackAdapter(val context: Context, val items: MutableList<UserInfoData>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    //북마크 key값을 담을 리스트
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val itemsData = items[position]

        // 기본 정보 초기화
        binding.itemName.text = itemsData.nickName
        binding.itemAdress.text = itemsData.address
        // uid값에 따른 사진 로드
        val pictureRef = Firebase.storage.reference.child("${itemsData.uid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                // Glide 를 통하여 imageView에 사진 로드
                Glide.with(context).load(it.result).into(binding.profileImageArea)
            }
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val isLiked = pref.getBoolean("isLiked_${itemsData.uid}", false)
        updateLikeButtonState(binding.btnLike, isLiked)

// 버튼을 클릭할 때의 동작입니다.
        binding.btnLike.setOnClickListener {
            val database = Firebase.database.reference.child("likePeople")
            val currentUserUid = Firebase.auth.currentUser!!.uid

            val newValue: Boolean = !isLiked // 새로운 값은 현재 값의 반대입니다.

            if (newValue) {
                // 좋아요 추가
                database.child(currentUserUid).child(itemsData.uid!!)
                    .setValue(true)
                    .addOnSuccessListener {
                        // SharedPreferences에 새로운 값을 저장합니다.
                        pref.edit().putBoolean("isLiked_${itemsData.uid}", true).apply()

                        // 버튼 이미지를 업데이트합니다.
                        updateLikeButtonState(binding.btnLike, true)
                    }
            } else {
                // 좋아요 취소
                database.child(currentUserUid).child(itemsData.uid!!)
                    .removeValue()
                    .addOnSuccessListener {
                        // SharedPreferences에서 값을 제거합니다.
                        pref.edit().remove("isLiked_${itemsData.uid}").apply()

                        // 버튼 이미지를 업데이트합니다.
                        updateLikeButtonState(binding.btnLike, false)
                    }
            }
        }

        // 채팅하기 버튼 누를 시 이벤트 처리
        binding.btnChat.setOnClickListener {
            // 채팅방 이동 함수54
            addChatRoom(position)
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {}

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

    private fun updateLikeButtonState(button: ImageView, isLiked: Boolean) {
        button.setImageResource(if (isLiked) R.drawable.favorite_pink else R.drawable.baseline_favorite_24)
    }
}