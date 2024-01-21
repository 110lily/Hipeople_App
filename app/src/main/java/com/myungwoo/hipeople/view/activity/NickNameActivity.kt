package com.myungwoo.hipeople.view.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.myungwoo.hipeople.firebase.FBAuth
import com.myungwoo.hipeople.firebase.FBRef
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ActivityNickNameBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage

import java.io.ByteArrayOutputStream

class NickNameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNickNameBinding
    private lateinit var id: String
    private lateinit var pw: String
    private lateinit var gender: String
    private lateinit var purpose: String
    private lateinit var birth: String
    private lateinit var height: String
    private lateinit var weight: String
    private lateinit var address: String
    private lateinit var edu: String
    private lateinit var token: String
    private var imageUri: Uri? = null
    private var key: String? = null
    private var userFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNickNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            token = task.result
        })

        id = intent.getStringExtra("id")!!
        pw = intent.getStringExtra("pw")!!
        gender = intent.getStringExtra("gender")!!
        purpose = intent.getStringExtra("purpose")!!
        birth = intent.getStringExtra("birth")!!
        height = intent.getStringExtra("height")!!
        weight = intent.getStringExtra("weight")!!
        address = intent.getStringExtra("address")!!
        edu = intent.getStringExtra("edu")!!

        binding.imageView2.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.edtRegisterNickName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (!(s.isNullOrBlank()) && s.matches("^[가-힣ㄱ-ㅎa-zA-Z0-9._-]{2,}\$".toRegex())) {
                    nickNameCheckFirebase(s.toString())
                } else {
                    binding.tvAno2.text = "한글/영어/숫자/밑줄을 사용할 수 있습니다."
                    binding.tvAno2.setTextColor(Color.BLACK)
                    binding.btnCompleteRegister.isEnabled = false
                }
            }
        })

        binding.btnCompleteRegister.setOnClickListener {
            val id = intent.getStringExtra("id")
            val pw = intent.getStringExtra("pw")
            val nickName = binding.edtRegisterNickName.text.toString()
            val key = FBRef.userRef.push().key.toString()
            this.key = key

            if (imageUri == null) {
                Toast.makeText(this, "프로필 사진을 선택해주세요.", Toast.LENGTH_SHORT).show()
                binding.btnCompleteRegister.isEnabled = false
            } else {
                signUp(id!!, pw!!, nickName)
            }
        }
    }

    private fun userPictureUpload(uid: String) {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val mountainsRef = storageRef.child("$uid.png")

        binding.imageView2.isDrawingCacheEnabled = true
        binding.imageView2.buildDrawingCache()
        val drawable = binding.imageView2.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = mountainsRef.putBytes(data)
            uploadTask.addOnFailureListener {
            }.addOnSuccessListener {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 1) {
            binding.imageView2.setImageURI(data?.data)
            imageUri = data?.data
            binding.btnCompleteRegister.isEnabled = true
        }
    }

    private fun signUp(email: String, password: String, name: String) {
        FBAuth.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val communityNickName = "익명${(Math.random() * 100).toInt()}"
                    try {
                        val uid = FBAuth.getUid()
                        val nickName = binding.edtRegisterNickName.text.toString()
                        FirebaseDatabase.getInstance().getReference("User").child("users")
                            .child(uid).setValue(
                                UserInfoData(
                                    uid, id, pw, gender, purpose, birth, height, weight, address, edu, nickName, "", 0, communityNickName, token
                                )
                            )
                        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@NickNameActivity, MainActivity::class.java)
                        startActivity(intent)
                        userPictureUpload(uid)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "화면 이동 중 문제 발생", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun nickNameCheckFirebase(nickName: String) {
        Firebase.database.reference.child("User").child("users").orderByChild("userNickname")
            .equalTo(nickName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        userFlag = true
                        binding.tvAno2.visibility = View.VISIBLE
                        binding.tvAno2.text = "한글/영어/숫자/밑줄을 사용할 수 있습니다."
                        binding.tvAno2.setTextColor(Color.BLUE)
                        binding.btnCompleteRegister.isEnabled = true
                    } else if (snapshot.value != null) { // 닉네임이 있는 경우
                        binding.tvAno2.visibility = View.VISIBLE
                        binding.tvAno2.text = "중복된 닉네임입니다."
                        binding.tvAno2.setTextColor(Color.RED)
                        userFlag = false
                        binding.btnCompleteRegister.isEnabled = false
                    } else {
                        binding.tvAno2.visibility = View.INVISIBLE
                        binding.tvAno2.text = "한글/영어/숫자/밑줄을 사용할 수 있습니다."
                        binding.tvAno2.setTextColor(Color.BLACK)
                        userFlag = false
                        binding.btnCompleteRegister.isEnabled = false
                    }
                }
            })
    }
}