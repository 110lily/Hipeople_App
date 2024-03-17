package com.myungwoo.hipeople.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ActivityChangeBinding

class ChangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeBinding
    private lateinit var dataList: MutableList<UserInfoData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataList = mutableListOf()

        val userId = Firebase.auth.currentUser?.uid ?: ""
        val database = Firebase.database.reference.child("User").child("users").child(userId)
        val userData = intent.getSerializableExtra("userData") as UserInfoData

        binding.tvNickname.text = userData.nickName
        binding.tvBirth.setText(userData.birth.toString())
        binding.tvPurpose.setText(userData.purpose.toString())
        binding.tvAddress.setText(userData.address.toString())
        binding.tvEdu.setText(userData.edu.toString())
        binding.tvHeight.setText(userData.height.toString())
        binding.tvWeight.setText(userData.weight.toString())

        binding.button.setOnClickListener {
            val user = mutableMapOf<String, Any>()
            val birth = binding.tvBirth.text.toString()
            val purpose = binding.tvPurpose.text.toString()
            val address = binding.tvAddress.text.toString()
            val edu = binding.tvEdu.text.toString()
            val height = binding.tvHeight.text.toString()
            val weight = binding.tvWeight.text.toString()

            user["birth"] = birth
            user["purpose"] = purpose
            user["address"] = address
            user["edu"] = edu
            user["height"] = height
            user["weight"] = weight

            database.updateChildren(user)
        }
    }
}
