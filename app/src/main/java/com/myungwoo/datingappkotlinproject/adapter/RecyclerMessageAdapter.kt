package com.myungwoo.datingappkotlinproject.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.myungwoo.datingappkotlinproject.R
import com.myungwoo.datingappkotlinproject.view.activity.ChatRoomActivity
import com.myungwoo.datingappkotlinproject.databinding.ListTalkItemMineBinding
import com.myungwoo.datingappkotlinproject.databinding.ListTalkItemOthersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.myungwoo.datingappkotlinproject.data.MessageData

class RecyclerMessagesAdapter(
    val context: Context,
    var chatRoomKey: String?,
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var messages: ArrayList<MessageData> = arrayListOf()
    var messageKeys: ArrayList<String> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val recyclerView = (context as ChatRoomActivity).recyclerTalks

    init {
        getMessages()
    }

    fun getMessages() {
        // RealtimeDatabase에서 메세지 목록 가져오기
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
                    recyclerView.scrollToPosition(messages.size - 1) // 가장 최근 메시지로 이동
                }
            })
    }

    override fun getItemViewType(position: Int): Int {               //메시지의 id에 따라 내 메시지/상대 메시지 구분
        return if (messages[position].senderUid.equals(myUid)) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {            //메시지가 내 메시지인 경우
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.list_talk_item_mine, parent, false)   //내 메시지 레이아웃으로 초기화

                MyMessageViewHolder(ListTalkItemMineBinding.bind(view))
            }
            else -> {      //메시지가 상대 메시지인 경우
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.list_talk_item_others, parent, false)  //상대 메시지 레이아웃으로 초기화
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

    inner class OtherMessageViewHolder(itemView: ListTalkItemOthersBinding) :         //상대 메시지 뷰홀더
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
            var message = messages[position]
            var sendDate = message.sendedDate

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            // ValueEventListener를 제거 (재사용될 때를 대비하여)
            if (valueEventListener != null) {
                messageRef.child(messageKeys[position]).removeEventListener(valueEventListener!!)
            }
            // ValueEventListener를 생성 및 추가
            valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val confirmed = snapshot.child("confirmed").getValue(Boolean::class.java) ?: false
                    txtIsShown.visibility = if (confirmed) View.GONE else View.VISIBLE
                    if (!confirmed) { // 만약 메시지가 아직 읽지 않았다면
                        setShown(position) // 메시지를 읽었음을 표시
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
            messageRef.child(messageKeys[position]).addValueEventListener(valueEventListener!!)
        }


        fun getDateText(sendDate: String): String {    //메시지 전송 시각 생성
            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                var hour = timeString.substring(0, 2)
                var minute = timeString.substring(2, 4)

                var timeformat = "%02d:%02d"

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

        fun setShown(position: Int) {          //메시지 확인하여 서버로 전송
            FirebaseDatabase.getInstance().getReference("ChatRoom")
                .child("chatRooms").child(chatRoomKey!!).child("messages")
                .child(messageKeys[position]).child("confirmed").setValue(true)
                .addOnSuccessListener {
                    Log.i("checkShown", "환영합니다")
                }
        }
    }

    inner class MyMessageViewHolder(itemView: ListTalkItemMineBinding) :       // 내 메시지용 ViewHolder
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

            // ValueEventListener를 제거 (재사용될 때를 대비하여)
            valueEventListener?.let {
                messageRef.child(messageKeys[position]).removeEventListener(it)
            }

            // ValueEventListener를 생성 및 추가
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

        fun getDateText(sendDate: String): String {        //메시지 전송 시각 생성
            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                var hour = timeString.substring(0, 2)
                var minute = timeString.substring(2, 4)

                var timeformat = "%02d:%02d"

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