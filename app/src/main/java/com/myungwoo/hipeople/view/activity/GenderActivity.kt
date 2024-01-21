package com.myungwoo.hipeople.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.databinding.ActivityGenderBinding

class GenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderBinding
    private lateinit var gender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        binding = ActivityGenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnMan.setOnClickListener {
            gender = "남자"
            binding.btnMan.setBackgroundColor(getColor(R.color.btncilck))
            binding.btnWoman.setBackgroundColor(getColor(R.color.btncolor))
        }

        binding.btnWoman.setOnClickListener {
            gender = "여자"
            binding.btnMan.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnWoman.setBackgroundColor(getColor(R.color.btncilck))
        }

        binding.btnNext2.setOnClickListener {
            val intent = Intent(this, PurposeActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            startActivity(intent)
        }
    }
}