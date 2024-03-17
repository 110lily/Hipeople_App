package com.myungwoo.hipeople.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myungwoo.hipeople.data.ChatRoomData
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.LikepeopleItemBinding
import com.myungwoo.hipeople.view.activity.ChatActivity

class LikePeopleAdapter(val context: Context, private val items: MutableList<UserInfoData>) :
    RecyclerView.Adapter<LikePeopleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LikepeopleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val binding = holder.binding
        val itemsData = items[position]
        binding.tvName.text = items[position].nickName
        val pictureRef = Firebase.storage.reference.child("${itemsData.uid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(context).load(it.result).into(binding.ivProfile)
            }
        }
        binding.chatBtn.setOnClickListener {
            addChatRoom(position)
        }
    }

    inner class ViewHolder(val binding: LikepeopleItemBinding) : RecyclerView.ViewHolder(binding.root)

    private fun addChatRoom(position: Int) {
        val opponent = items[position]
        val currentUserUid = Firebase.auth.currentUser!!.uid
        val database =
            FirebaseDatabase.getInstance().getReference("ChatRoom")
        val chatRoom = ChatRoomData(
            mapOf(Firebase.auth.currentUser!!.uid to (true), opponent.uid!! to (true)),
            mapOf(
                "${currentUserUid}${opponent.uid}" to (true),
                "${opponent.uid}${currentUserUid}" to (true)
            ),
            null
        )

        database.child("chatRooms")
            .orderByChild("users2/${opponent.uid}${currentUserUid}")
            .equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        database.child("chatRooms").push().setValue(chatRoom)
                            .addOnSuccessListener {
                                goToChatRoom(chatRoom, opponent, "")
                            }
                    } else {
                        goToChatRoom(chatRoom, opponent, "")
                    }
                }
            })
    }

    fun goToChatRoom(
        chatRoom: ChatRoomData,
        opponentUid: UserInfoData,
        chatRoomKey: String
    ) {
        var intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("ChatRoom", chatRoom)
        intent.putExtra("Opponent", opponentUid)
        intent.putExtra("ChatRoomKey", "")
        context.startActivity(intent)
    }
}