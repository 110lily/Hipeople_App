package com.myungwoo.hipeople.view.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.data.UserInfoData
import com.myungwoo.hipeople.databinding.ActivityChangeBinding

class ChangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeBinding
    private lateinit var userData: UserInfoData
    private val userId = Firebase.auth.currentUser?.uid ?: ""
    private val userDatabaseReference = Firebase.database.reference.child("User").child("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()
        binding.button.setOnClickListener {
            changeData()
        }
    }

    private fun loadData() {
        userDatabaseReference.orderByChild("uid")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        userData = data.getValue(UserInfoData::class.java)!!
                        binding.tvNickname.text = userData.nickName
                        binding.tvBirth.setText(userData.birth.toString())
                        binding.tvPurpose.setText(userData.purpose.toString())
                        binding.tvAddress.setText(userData.address.toString())
                        binding.tvEdu.setText(userData.edu.toString())
                        binding.tvHeight.setText(userData.height.toString())
                        binding.tvWeight.setText(userData.weight.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun changeData() {
        val user = mutableMapOf<String, Any>()
        user["birth"] = binding.tvBirth.text.toString()
        user["purpose"] = binding.tvPurpose.text.toString()
        user["address"] = binding.tvAddress.text.toString()
        user["edu"] = binding.tvEdu.text.toString()
        user["height"] = binding.tvHeight.text.toString()
        user["weight"] = binding.tvWeight.text.toString()

        userDatabaseReference.child(userId).updateChildren(user)
        Toast.makeText(this, R.string.change_succes, Toast.LENGTH_SHORT).show()
    }
}
