package com.myungwoo.hipeople.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.dao.PostingDAO
import com.myungwoo.hipeople.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var spf: SharedPreferences
    private var currentUserUid = Firebase.auth.currentUser!!.uid

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userData = intent.getSerializableExtra("dataList") as UserInfoData
        binding.tvNickname.text = userData.nickName
        val postingDAO = PostingDAO()
        val pictureRef = postingDAO.storage!!.reference.child("${currentUserUid}.png")
        pictureRef.downloadUrl.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.e("pictureAdapter", "Success")
                Glide.with(applicationContext).load(it.result).into(binding.circleImageView)
            }
        }

        binding.profileChange.setOnClickListener {
            val intent = Intent(this, ChangeActivity::class.java)
            intent.putExtra("userData", userData)
            startActivity(intent)
        }

        binding.btnSignout.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("로그아웃 하시겠습니까?")
                .setPositiveButton("확인") { dialog, _ ->
                    spf = getSharedPreferences("loginKeep", Context.MODE_PRIVATE)
                    val spfEdit = spf.edit()
                    spfEdit.putBoolean("isLogin", false)
                    spfEdit.apply()
                    try {
                        Firebase.auth.signOut()
                        startActivity(Intent(this, MainActivity::class.java))
                        dialog.dismiss()
                        finish()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        dialog.dismiss()
                        Toast.makeText(this, "로그아웃 중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show()
                    }
                }.setNegativeButton("취소") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.show()
        }

        binding.btnRemove.setOnClickListener {
            val builder = AlertDialog.Builder(this)
                .setTitle("회원탈퇴")
                .setMessage("정말 회원탈퇴 하시겠습니까?")
                .setPositiveButton("확인") { dialog, id ->
                    spf = getSharedPreferences("loginKeep", Context.MODE_PRIVATE)
                    val spfEdit = spf.edit()
                    spfEdit.putBoolean("isLogin", false)
                    spfEdit.apply()
                    Toast.makeText(this, "그동안 이용해 주셔서 감사합니다.", Toast.LENGTH_SHORT).show()
                    try {
                        Firebase.database.reference.child("User").child("users")
                            .child(currentUserUid).setValue(null)
                            .addOnCompleteListener { Log.e("ProfileActivity", "회원 탈퇴 성공") }
                            .addOnFailureListener {
                                Log.e("ProfileActivity", "회원 탈퇴 실패  /  ${it.message}")
                            }
                        dialog.dismiss()
                        finish()
                        startActivity(Intent(this, MainActivity::class.java))
                        FirebaseAuth.getInstance().currentUser!!.delete()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        dialog.dismiss()
                    }
                }.setNegativeButton("취소") { dialog, id ->
                    dialog.dismiss()
                }
            builder.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, AppMainActivity::class.java))
        finish()
    }
}