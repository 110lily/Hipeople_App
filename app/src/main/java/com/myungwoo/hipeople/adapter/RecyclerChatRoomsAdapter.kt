package com.myungwoo.hipeople.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ListChatroomItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myungwoo.hipeople.data.ChatRoomData
import com.myungwoo.hipeople.view.activity.ChatRoomActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerChatRoomsAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerChatRoomsAdapter.ViewHolder>() {
    var chatRooms: ArrayList<ChatRoomData> = arrayListOf()
    var chatRoomKeys: ArrayList<String> = arrayListOf()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    init {
        try {
            setupAllUserList()
        } catch (e: Exception) {
            Log.e("RecyclerChatRoomsAdapter", "${e.message}")
            Log.e("RecyclerChatRoomsAdapter", "${e.printStackTrace()}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_chatroom_item, parent, false)
        return ViewHolder(ListChatroomItemBinding.bind(view))
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userIdList = chatRooms[position].users!!.keys
        val opponent2 = userIdList.last { !it.equals(myUid) }
        val pictureRef = Firebase.storage.reference.child("${opponent2}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(context).load(it.result).into(holder.ivProfile)
            }
        }
        try {
            val chatUserList: MutableList<UserInfoData> = mutableListOf()
            FirebaseDatabase.getInstance().getReference("User").child("users")
                .orderByChild("uid")
                .equalTo(opponent2)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (data in snapshot.children) {
                            holder.chatRoomKey = data.key.toString()
                            val chatUser = data.getValue<UserInfoData>()!!
                            chatUserList.add(chatUser)
                            holder.opponentChatUser = chatUser
                            holder.tvName.text = chatUser.nickName.toString()

                        }
                    }
                })
        } catch (e: java.lang.Exception) {
            Log.e("RecyclerChatRoomsAdapter", "${e.message}")
            Log.e("RecyclerChatRoomsAdapter", "${e.printStackTrace()}")
        }
        holder.background.setOnClickListener()
        {
            try {
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("ChatRoom", chatRooms.get(position))
                intent.putExtra("Opponent", holder.opponentChatUser)
                intent.putExtra("ChatRoomKey", chatRoomKeys[position])
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("RecyclerChatRoomsAdapter", "${e.message}")
                Log.e("RecyclerChatRoomsAdapter", "${e.printStackTrace()}")
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "채팅방 이동 중 문제가 발생하였습니다. ${e.printStackTrace()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (chatRooms[position].messages!!.size > 0) {
            setupLastMessageAndDate(holder, position)
            setupMssageCount(holder, position)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupLastMessageAndDate(
        holder: ViewHolder,
        position: Int
    ) {
        try {
            val lastMessage =
                chatRooms[position].messages!!.values.sortedWith(compareBy { it.sendedDate })
                    .last()
            holder.tvMessage.text = lastMessage.content
            holder.tvDate.text =
                getLastMessageTimeString(lastMessage.sendedDate)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setupMssageCount(holder: ViewHolder, position: Int) {
        try {
            val unconfirmedCount = chatRooms[position].messages!!.filter {
                !it.value.confirmed && !it.value.senderUid.equals(myUid)
            }.size
            if (unconfirmedCount > 0) {
                holder.tvChatCount.visibility = View.VISIBLE
                holder.tvChatCount.text = unconfirmedCount.toString()
            } else {
                holder.tvChatCount.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            holder.tvChatCount.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastMessageTimeString(lastTimeString: String): String {
        try {
            val currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId())
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val messageMonth = lastTimeString.substring(4, 6).toInt()
            val messageDate = lastTimeString.substring(6, 8).toInt()
            val messageHour = lastTimeString.substring(8, 10).toInt()
            val messageMinute = lastTimeString.substring(10, 12).toInt()
            val formattedCurrentTimeString = currentTime.format(dateTimeFormatter)
            val currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
            val currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
            val currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
            val currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()
            val monthAgo = currentMonth - messageMonth
            val dayAgo = currentDate - messageDate
            val hourAgo = currentHour - messageHour
            val minuteAgo = currentMinute - messageMinute
            if (monthAgo > 0) {
                return monthAgo.toString() + "개월 전"
            } else {
                if (dayAgo > 0) {
                    if (dayAgo == 1) {
                        return "어제"
                    } else {
                        return dayAgo.toString() + "일 전"
                    }
                } else {
                    if (hourAgo > 0) {
                        return hourAgo.toString() + "시간 전"
                    } else {
                        if (minuteAgo > 0) {
                            return minuteAgo.toString() + "분 전"
                        } else {
                            return "방금"
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return ""
        }
    }

    inner class ViewHolder(itemView: ListChatroomItemBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var opponentChatUser = UserInfoData()
        var chatRoomKey = ""
        var background = itemView.background
        var tvName = itemView.tvName
        var tvMessage = itemView.tvMessage
        var tvDate = itemView.tvDate
        var tvChatCount = itemView.tvChatCount
        var ivProfile = itemView.ivProfile
    }

    private fun setupAllUserList() {
        FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
            .orderByChild("users/$myUid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatRooms.clear()
                    for (data in snapshot.children) {
                        chatRooms.add(data.getValue<ChatRoomData>()!!)
                        chatRoomKeys.add(data.key!!)
                    }
                    notifyDataSetChanged()
                }
            })
    }
}