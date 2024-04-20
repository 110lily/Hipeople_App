package com.myungwoo.hipeople.adapter

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.data.ChatRoomData
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ItemPhotoBinding
import com.myungwoo.hipeople.view.activity.ChatActivity

class CardStackAdapter(val context: Context, private val items: MutableList<UserInfoData>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val itemsData = items[position]

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val isLiked = pref.getBoolean("isLiked_${itemsData.uid}", false)
        updateLikeButtonState(binding.btnLike, isLiked)

        binding.itemName.text = itemsData.nickName
        binding.itemAdress.text = itemsData.address
        val pictureRef = Firebase.storage.reference.child("${itemsData.uid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(context).load(it.result).into(binding.profileImageArea)
            }
        }

        binding.btnLike.setOnClickListener {
            val isLiked = pref.getBoolean("isLiked_${itemsData.uid}", false)
            val database = Firebase.database.reference.child("likePeople")
            val currentUserUid = Firebase.auth.currentUser!!.uid
            val newValue: Boolean = !isLiked

            if (newValue) {
                database.child(currentUserUid).child(itemsData.uid!!)
                    .setValue(true)
                    .addOnSuccessListener {
                        pref.edit().putBoolean("isLiked_${itemsData.uid}", true).apply()
                        updateLikeButtonState(binding.btnLike, true)
                    }
            } else {
                database.child(currentUserUid).child(itemsData.uid!!)
                    .removeValue()
                    .addOnSuccessListener {
                        pref.edit().remove("isLiked_${itemsData.uid}").apply()
                        updateLikeButtonState(binding.btnLike, false)
                    }
            }
        }

        binding.btnChat.setOnClickListener {
            addChatRoom(position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)

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

    private fun updateLikeButtonState(button: ImageView, isLiked: Boolean) {
        Log.d("LikeButton", "isLiked before: $isLiked")
        button.setImageResource(if (isLiked) R.drawable.favorite_pink else R.drawable.baseline_favorite_24)
    }
}