package com.myungwoo.hipeople.view.activity

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.myungwoo.hipeople.adapter.RecyclerMessageAdapter
import com.myungwoo.hipeople.data.ChatRoomData
import com.myungwoo.hipeople.data.MessageData
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ActivityChatBinding
import com.myungwoo.hipeople.fcm.NotiModel
import com.myungwoo.hipeople.fcm.PushNotification
import com.myungwoo.hipeople.fcm.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

class ChatActivity : AppCompatActivity() {

    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }
    private lateinit var btnExit: ImageButton
    private lateinit var btnSubmit: Button
    private lateinit var tvTitle: TextView
    private lateinit var edtMessage: TextView
    private lateinit var firebaseDatabase: DatabaseReference
    lateinit var recyclerTalks: RecyclerView
    private lateinit var chatRoom: ChatRoomData
    private lateinit var opponentChatUser: UserInfoData
    private lateinit var chatRoomKey: String
    private lateinit var myUid: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnSubmit.isEnabled = s.toString() != ""
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        initializeProperty()
        initializeView()
        initializeListener()
        setupChatRooms()
    }

    private fun initializeProperty() {
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!
        firebaseDatabase = FirebaseDatabase.getInstance().reference!!
        chatRoom = (intent.getSerializableExtra("ChatRoom")) as ChatRoomData
        chatRoomKey = intent.getStringExtra("ChatRoomKey")!!
        opponentChatUser = (intent.getSerializableExtra("Opponent")) as UserInfoData
    }

    private fun initializeView() {
        btnExit = binding.imgbtnQuit
        edtMessage = binding.edtMessage
        recyclerTalks = binding.recyclerMessage
        btnSubmit = binding.btnSubmit
        tvTitle = binding.tvTitle
        tvTitle.text = opponentChatUser.nickName ?: ""

        val pictureRef = Firebase.storage.reference.child("${opponentChatUser.uid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Glide.with(this).load(it.result).into(binding.ivPicture)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeListener() {
        btnSubmit.setOnClickListener {
            putMessage()
            val notiModel = NotiModel("새로운 메시지가 도착했습니다.", binding.edtMessage.text.toString())
            val pushModel = PushNotification(notiModel, opponentChatUser.token.toString())
            Log.e("putMessage", opponentChatUser.token.toString())
            testPush(pushModel)
        }
    }

    private fun setupChatRooms() {
        if (chatRoomKey.isBlank()) {
            setupChatRoomKey()
        } else {
            setupRecycler()
        }
    }

    private fun setupChatRoomKey() {
        FirebaseDatabase.getInstance().getReference("ChatRoom")
            .child("chatRooms")
            .orderByChild("users2/${opponentChatUser.uid}${Firebase.auth.currentUser!!.uid}")
            .equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        chatRoomKey = data.key!!
                        setupRecycler()
                        break
                    }
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun putMessage() {
        try {
            val message =
                MessageData(myUid, getDateTimeString(), edtMessage.text.toString())
            FirebaseDatabase.getInstance().getReference("ChatRoom").child("chatRooms")
                .child(chatRoomKey).child("messages")
                .push().setValue(message).addOnSuccessListener {
                    Log.e("putMessage", "메시지 전송에 성공하였습니다.")
                    edtMessage.text = ""
                }.addOnCanceledListener {
                    Log.i("putMessage", "메시지 전송에 실패하였습니다")
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("putMessage", "메시지 전송 중 오류가 발생하였습니다.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDateTimeString(): String {
        try {
            val localDateTime = LocalDateTime.now()
            localDateTime.atZone(TimeZone.getDefault().toZoneId())
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return localDateTime.format(dateTimeFormatter).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.Exception("getTimeError")
        }
    }

    private fun setupRecycler() {
        recyclerTalks.layoutManager = LinearLayoutManager(this)
        recyclerTalks.adapter = RecyclerMessageAdapter(this, chatRoomKey)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun testPush(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        RetrofitInstance.api.postNotification(notification)
    }
}