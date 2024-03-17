package com.myungwoo.hipeople.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.databinding.ActivityEduBinding

class EduActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEduBinding
    private lateinit var edu: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEduBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")
        val birth = intent.getStringExtra("birth")
        val height = intent.getStringExtra("height")
        val weight = intent.getStringExtra("weight")
        val address = intent.getStringExtra("address")

        binding.btnHighSchool.setOnClickListener {
            edu = "고등학교"
            buttonSelected(edu)
        }

        binding.btnCollege.setOnClickListener {
            edu = "전문대"
            buttonSelected(edu)
        }

        binding.btnUniversity.setOnClickListener {
            edu = "대학교"
            buttonSelected(edu)
        }

        binding.btnGraduateSchool.setOnClickListener {
            edu = "대학원"
            buttonSelected(edu)
        }

        binding.btnEtc.setOnClickListener {
            edu = "기타"
            buttonSelected(edu)
        }

        binding.btnNext7.setOnClickListener {
            val intent = Intent(this, NickNameActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birth)
            intent.putExtra("height", height)
            intent.putExtra("weight", weight)
            intent.putExtra("address", address)
            intent.putExtra("edu", edu)
            startActivity(intent)
        }
    }

    private fun buttonSelected(edu: String) {
        binding.btnHighSchool.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnCollege.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnUniversity.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnGraduateSchool.setBackgroundColor(getColor(R.color.btncolor))
        binding.btnEtc.setBackgroundColor(getColor(R.color.btncolor))
        when (edu) {
            "고등학교" -> binding.btnHighSchool.setBackgroundColor(getColor(R.color.btncilck))
            "전문대" -> binding.btnCollege.setBackgroundColor(getColor(R.color.btncilck))
            "대학교" -> binding.btnUniversity.setBackgroundColor(getColor(R.color.btncilck))
            "대학원" -> binding.btnGraduateSchool.setBackgroundColor(getColor(R.color.btncilck))
            "기타" -> binding.btnEtc.setBackgroundColor(getColor(R.color.btncilck))
        }
    }
}