package com.myungwoo.datingappkotlinproject.view.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.myungwoo.datingappkotlinproject.R
import com.myungwoo.datingappkotlinproject.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var spf: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        spf = getSharedPreferences("loginKeep", Context.MODE_PRIVATE)
        if (spf.getBoolean("isLogin", false)) {
            val intent = Intent(this, AppMainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnLogin -> {
                confirmLogin()
            }

            R.id.btnSignUp -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }
    }

    private fun confirmLogin() {
        try {
            if (binding.edtId.text.toString().isBlank() && binding.edtPassword.text.toString()
                    .isBlank()
            ) {
                Toast.makeText(this, "아이디 또는 패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // Firebase Auth의 함수를 이용하여 Id값과 Pw가 일치하는지 확인
                auth.signInWithEmailAndPassword(
                    binding.edtId.text.toString(),
                    binding.edtPassword.text.toString()
                )
                    // 성공시 콜백함수
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "성공", Toast.LENGTH_SHORT).show()
                            Log.e("MainActivity", "로그인 성공")
                            // 자동 로그인을 위한 SharedPreference에 Boolean값 저장 로그인이 성공하면 true 아니면 false
                            val spfEdit = spf.edit()
                            spfEdit.putBoolean("isLogin", true)
                            spfEdit.apply()
                            val intent = Intent(
                                this,
                                AppMainActivity::class.java
                            )
                            startActivity(intent)
                            finish()
                        } else {
                            // 로그인 실패 시 아이디가 잘못 입력 되었는지, 패스워드가 잘못 입력되었는지 Firebase RealtimeDatabase에서 확인
                            val id = binding.edtId.text.toString()
                            Firebase.database.reference.child("User").child("users").orderByChild("id")
                                .equalTo(id)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        Log.e("ddddddddddddddd", snapshot.value.toString())
                                        if (snapshot.value != null) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "패스워드를 확인해주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "가입된 정보가 없습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(this, "아이디 또는 패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

//    // 위치 정보 권한 요청 로직 실행
//    private fun requestLocationPermission() {
//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED &&
//            ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            // 이미 퍼미션을 가지고 있는 경우 처리할 로직 작성
//
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                PERMISSIONS_REQUEST_CODE
//            )
//        }
//    }
//
//// 거부 처리 로직 실행
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // 퍼미션 동의 시 처리할 로직 작성
//
//            } else {
//                showToastMessage("위치 정보 권한이 거부되었습니다.")
//            }
//        }
//    }
//
//// 토스트 메시지 출력 함수
//
//    private fun showToastMessage(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
}