package com.myungwoo.hipeople.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myungwoo.hipeople.databinding.ActivityHeightBinding

class HeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")
        val birth = intent.getStringExtra("birth")

        binding.btnNext5.setOnClickListener {
            val height = binding.edtHeight.text.toString()
            val weight = binding.edtWeight.text.toString()
            val intent = Intent(this, AddressActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birth)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            startActivity(intent)
        }
    }
}