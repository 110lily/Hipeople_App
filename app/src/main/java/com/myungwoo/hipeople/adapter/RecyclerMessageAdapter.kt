package com.myungwoo.hipeople.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.view.activity.ChatRoomActivity
import com.myungwoo.hipeople.databinding.ListTalkItemMineBinding
import com.myungwoo.hipeople.databinding.ListTalkItemOthersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.myungwoo.hipeople.data.MessageData

class RecyclerMessagesAdapter(
    val context: Context,
    var chatRoomKey: String?,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var messages: ArrayList<MessageData> = arrayListOf()
    var messageKeys: ArrayList<String> = arrayListOf()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val recyclerView = (context as ChatRoomActivity).recyclerTalks

    init {
        getMessages()
    }

    private fun getMessages() {
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms").child(chatRoomKey!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (data in snapshot.children) {
                        messages.add(data.getValue<MessageData>()!!)
                        messageKeys.add(data.key!!)
                    }
                    notifyDataSetChanged()
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            })
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderUid.equals(myUid)) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.list_talk_item_mine, parent, false)

                MyMessageViewHolder(ListTalkItemMineBinding.bind(view))
            }

            else -> {
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.list_talk_item_others, parent, false)
                OtherMessageViewHolder(ListTalkItemOthersBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messages[position].senderUid.equals(myUid)) {
            (holder as MyMessageViewHolder).bind(position)
        } else {
            (holder as OtherMessageViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class OtherMessageViewHolder(itemView: ListTalkItemOthersBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown

        private val messageRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("ChatRoom")
                .child("chatRooms").child(chatRoomKey!!).child("messages")

        private var valueEventListener: ValueEventListener? = null

        fun bind(position: Int) {
            val message = messages[position]
            val sendDate = message.sendedDate

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            if (valueEventListener != null) {
                messageRef.child(messageKeys[position]).removeEventListener(valueEventListener!!)
            }
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val confirmed = snapshot.child("confirmed").getValue(Boolean::class.java) ?: false
                    txtIsShown.visibility = if (confirmed) View.GONE else View.VISIBLE
                    if (!confirmed) {
                        setShown(position)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            messageRef.child(messageKeys[position]).addValueEventListener(valueEventListener!!)
        }


        fun getDateText(sendDate: String): String {
            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                val hour = timeString.substring(0, 2)
                val minute = timeString.substring(2, 4)
                val timeformat = "%02d:%02d"
                if (hour.toInt() > 11) {
                    dateText += "오후 "
                    dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
                } else {
                    dateText += "오전 "
                    dateText += timeformat.format(hour.toInt(), minute.toInt())
                }
            }
            return dateText
        }

        fun setShown(position: Int) {
            FirebaseDatabase.getInstance().getReference("ChatRoom")
                .child("chatRooms").child(chatRoomKey!!).child("messages")
                .child(messageKeys[position]).child("confirmed").setValue(true)
                .addOnSuccessListener {
                    Log.i("checkShown", "환영합니다")
                }
        }
    }

    inner class MyMessageViewHolder(itemView: ListTalkItemMineBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private var txtMessage = itemView.txtMessage
        private var txtDate = itemView.txtDate
        var background = itemView.background
        var txtIsShown = itemView.txtIsShown

        private val messageRef: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("ChatRoom")
                .child("chatRooms").child(chatRoomKey!!).child("messages")

        private var valueEventListener: ValueEventListener? = null

        fun bind(position: Int) {
            val message = messages[position]
            val sendDate = message.sendedDate
            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            valueEventListener?.let {
                messageRef.child(messageKeys[position]).removeEventListener(it)
            }

            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val confirmed = snapshot.child("confirmed").getValue(Boolean::class.java) ?: false
                    if (confirmed) {
                        txtIsShown.visibility = View.GONE
                    } else {
                        txtIsShown.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            messageRef.child(messageKeys[position]).addValueEventListener(valueEventListener!!)

        }

        private fun getDateText(sendDate: String): String {
            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                val hour = timeString.substring(0, 2)
                val minute = timeString.substring(2, 4)
                val timeformat = "%02d:%02d"

                if (hour.toInt() > 11) {
                    dateText += "오후 "
                    dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
                } else {
                    dateText += "오전 "
                    dateText += timeformat.format(hour.toInt(), minute.toInt())
                }
            }
            return dateText
        }
    }
}