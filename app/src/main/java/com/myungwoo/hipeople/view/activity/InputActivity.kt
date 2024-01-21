package com.myungwoo.hipeople.view.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.myungwoo.hipeople.databinding.ActivityInputBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*
import com.bumptech.glide.Glide
import com.myungwoo.hipeople.data.UserInfoData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.myungwoo.hipeople.data.AnonymousPostingData
import com.myungwoo.hipeople.dao.PostingDAO
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInputBinding
    private lateinit var dataList: MutableList<UserInfoData>
    private var imageUri: Uri? = null
    private val currentUser = Firebase.auth.currentUser!!.uid

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = mutableListOf()

        Firebase.database.reference.child("User").child("users").orderByChild("uid")
            .equalTo(currentUser)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val user = data.getValue(UserInfoData::class.java)!!
                        dataList.add(user)
                    }
                }
            })

        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                imageUri = it.data?.data
                Glide.with(applicationContext).load(imageUri).into(binding.ivAddPicture)
            }
        }

        binding.ivAddPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            requestLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            binding.btnSave.isEnabled = false
            if (binding.edtTitle.text.isNotEmpty() && binding.edtContent.text.isNotEmpty() && imageUri != null) {
                val postingDAO = PostingDAO()
                val docID = FirebaseDatabase.getInstance().getReference("board")?.push()?.key
                val author: String? = dataList.get(0).communityNickname
                val title = binding.edtTitle.text.toString()
                val content = binding.edtContent.text.toString().trim()
                val date = getDateTimeString()
                val anonymousPostingData =
                    AnonymousPostingData(docID!!, currentUser, author!!, title, content, 0, 0, 0, date)

                postingDAO.databaseReference?.child(docID)?.setValue(anonymousPostingData)
                    ?.addOnSuccessListener {
                        Log.e("PictureAddActivity", "이미지 정보 업로드 성공")
                        val pictureRef =
                            postingDAO.storage?.reference?.child("images/${docID}.png")
                        pictureRef!!.putFile(imageUri!!).addOnSuccessListener {// 성공시
                            Toast.makeText(
                                applicationContext,
                                "게시글이 등록되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PictureAddActivity", "이미지 업로드 성공")
                            finish()
                        }.addOnFailureListener {// 실패시
                            Toast.makeText(
                                applicationContext,
                                "게시글 등록 실패하였습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("PictureAddActivity", "이미지 업로드 실패")
                            binding.btnSave.isEnabled = true
                        }
                    }?.addOnFailureListener {// 실패시
                        Log.e("PictureAddActivity", "이미지 정보 업로드 실패")
                        binding.btnSave.isEnabled = true
                    }
            } else {
                Toast.makeText(applicationContext, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
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
}