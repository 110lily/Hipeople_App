package com.myungwoo.hipeople.view.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.showSnackBar

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
        binding.btnMainSignup.setOnClickListener(this)
        binding.btnMainLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_main_login -> {
                confirmLogin()
            }

            R.id.btn_main_signup -> {
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }
    }

    private fun confirmLogin() {
        try {
            if (binding.edtId.text.toString().isBlank() || binding.edtPassword.text.toString().isBlank()) {
                binding.clMain.showSnackBar(getString(R.string.main_error_empty))
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtId.text.toString()).matches()) {
                binding.clMain.showSnackBar(getString(R.string.main_error_email))
            } else {
                auth.signInWithEmailAndPassword(
                    binding.edtId.text.toString(),
                    binding.edtPassword.text.toString()
                )
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // 자동 로그인을 위한 SharedPreference에 Boolean값 저장 로그인이 성공하면 true 아니면 false
                            val spfEdit = spf.edit()
                            spfEdit.putBoolean("isLogin", true)
                            spfEdit.apply()
                            startActivity(Intent(this, AppMainActivity::class.java))
                            finish()
                        } else {
                            val id = binding.edtId.text.toString()
                            Firebase.database.reference.child("User").child("users").orderByChild("id")
                                .equalTo(id)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.value != null) {
                                            binding.clMain.showSnackBar(getString(R.string.main_error_password))
                                        } else {
                                            binding.clMain.showSnackBar(getString(R.string.main_error_id))
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }
            }
        } catch (e: java.lang.Exception) {
            Log.e("MainActivity", "error : $e ")
        }
    }
}