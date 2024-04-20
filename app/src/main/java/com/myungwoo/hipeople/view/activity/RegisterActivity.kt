package com.myungwoo.hipeople.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var id: String? = null
    private var pw: String? = null
    private var emailFlag = false
    private var passwordFlag = false
    private var flag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkIdChangedListener()
        checkPWChangedListener()
        checkPWConfirmChangedListener()
        successSignup()
    }

    private fun checkIdChangedListener() {
        binding.edtRegisterId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.groupRegister.visibility = View.INVISIBLE

                checkRegisterEmail(s.toString())
                checkIdFirebase(s.toString())

                if (emailFlag && flag) {
                    binding.tvRegisterIdConfirm.visibility = View.VISIBLE
                    binding.tvRegisterIdConfirm.text = getString(R.string.register_success_id)
                    binding.groupRegister.visibility = View.VISIBLE
                    binding.tvRegisterIdConfirm.setTextColor(Color.BLUE)
                } else if (emailFlag == false) {
                    binding.tvRegisterIdConfirm.visibility = View.VISIBLE
                    binding.tvRegisterIdConfirm.text = getString(R.string.register_error_id_pattern)
                    binding.tvRegisterIdConfirm.setTextColor(Color.RED)
                    binding.groupRegister.visibility = View.INVISIBLE
                } else if (flag == false) {
                    binding.groupRegister.visibility = View.INVISIBLE
                }
            }
        })
    }

    private fun checkRegisterEmail(email: String) {
        emailFlag = false
        if (email.isNotEmpty() && !email.contains("@") && email.length < 10) {
            emailFlag = false
            binding.tvRegisterIdConfirm.visibility = View.VISIBLE
            binding.tvRegisterIdConfirm.text = getString(R.string.register_error_id_pattern)
            binding.tvRegisterIdConfirm.setTextColor(Color.RED)
        } else if (email.isNotEmpty() && email.contains("@") && email.length > 10) {
            binding.tvRegisterIdConfirm.visibility = View.VISIBLE
            emailFlag = true
        }
    }

    private fun checkIdFirebase(id: String) {
        Firebase.database.reference.child("User").child("users").orderByChild("id")
            .equalTo(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        flag = true
                    } else if (snapshot.value != null) {
                        flag = false
                        binding.groupRegister.visibility = View.INVISIBLE
                        binding.tvRegisterIdConfirm.visibility = View.VISIBLE
                        binding.tvRegisterIdConfirm.text =
                            getString(R.string.register_error_id_exist)
                        binding.tvRegisterIdConfirm.setTextColor(Color.RED)
                    } else {
                        binding.tvRegisterIdConfirm.visibility = View.INVISIBLE
                        binding.tvRegisterIdConfirm.text = getString(R.string.register_id_input)
                    }
                }
            })
    }

    private fun checkPWChangedListener() {
        binding.edtRegisterPw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.tvRegisterPasswordConfirm.setTextColor(Color.GRAY)
                    binding.tvRegisterPasswordConfirm.text =
                        getString(R.string.register_password_confirm)
                    passwordFlag = false
                } else {
                    if (s!!.matches("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@$!%*#?&.])[A-Za-z[0-9]$@$!%*#?&.]{8,15}$".toRegex())) {
                        passwordFlag = true
                        binding.tvRegisterPasswordConfirm.setTextColor(Color.BLUE)
                        binding.tvRegisterPasswordConfirm.text =
                            getString(R.string.register_success_password)
                    } else {
                        binding.tvRegisterPasswordConfirm.setTextColor(Color.RED)
                    }
                }
                pw = s.toString()
            }
        })
    }

    private fun checkPWConfirmChangedListener() {
        binding.edtRegisterRePw.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (pw!!.matches(s.toString().toRegex()) && passwordFlag) {
                    binding.btnRegisterNext.visibility = View.VISIBLE
                    binding.tvRegisterPasswordCheckConfirm.visibility = View.INVISIBLE
                } else {
                    binding.btnRegisterNext.visibility = View.INVISIBLE
                    binding.tvRegisterPasswordCheckConfirm.visibility = View.VISIBLE
                    binding.tvRegisterPasswordCheckConfirm.setTextColor(Color.RED)

                }
            }
        })
    }

    private fun successSignup() {
        binding.btnRegisterNext.setOnClickListener {
            id = binding.edtRegisterId.text.toString()
            pw = binding.edtRegisterPw.text.toString()
            val intent = Intent(this, GenderActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}