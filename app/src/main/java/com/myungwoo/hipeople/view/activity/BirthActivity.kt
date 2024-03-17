package com.myungwoo.hipeople.view.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.myungwoo.hipeople.databinding.ActivityBirthBinding
import java.time.LocalDateTime

class BirthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBirthBinding
    private var yearFlag = false
    private var monthFlag = false
    private lateinit var year: String
    private lateinit var month: String
    private lateinit var day: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBirthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")
        val purpose = intent.getStringExtra("purpose")

        binding.edtRegisterBirthYear.setOnFocusChangeListener { _, hasFocus ->
            yearFlag = false
            year = binding.edtRegisterBirthYear.text.toString()
            if (year != "") {
                val yearInt = year.toInt()
                var yearIntFlag = false
                val today = LocalDateTime.now()
                val thisYear = today.year
                if (yearInt in 1900..thisYear) {
                    yearIntFlag = true
                }
                if (!hasFocus && year.isNotEmpty() && !yearIntFlag) {
                    Toast.makeText(this, "정확한 탄생년을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthYear.text.clear()
                } else if (year.isNotEmpty() && yearIntFlag) {
                    yearFlag = true
                }
            }
        }

        binding.edtRegisterBirthMonth.setOnFocusChangeListener { _, _ ->
            monthFlag = false
            month = binding.edtRegisterBirthMonth.text.toString()
            if (month != "") {
                val monthInt = month.toInt()
                var monthIntFlag = false
                if (monthInt in 1..12) {
                    monthIntFlag = true
                }
                if (!monthIntFlag && month.isNotEmpty()) {
                    Toast.makeText(this, "정확한 탄생월을 입력해 주세요", Toast.LENGTH_SHORT).show()
                    binding.edtRegisterBirthMonth.text.clear()
                } else if (monthIntFlag && month.isNotEmpty()) {
                    monthFlag = true
                }
            }
            if (month.length == 1) {
                month = "0$month"
            }
        }

        binding.btnNext4.setOnClickListener {
            day = binding.edtRegisterBirthDay.text.toString()
            if (day.length == 1) {
                day = "0$day"
            } else {
                day = binding.edtRegisterBirthDay.text.toString()
            }
            val birthDate = "${year}년 ${month}월 ${day}일"
            val intent = Intent(this, HeightActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            intent.putExtra("birth", birthDate)
            startActivity(intent)
        }
    }

    fun checkBirthDate(monthInt: Int, dayInt: Int): Boolean {
        var returnValue = false
        when (monthInt) {
            1 -> returnValue = checkBirthDateByMonth(3, dayInt)
            2 -> returnValue = checkBirthDateByMonth(1, dayInt)
            3 -> returnValue = checkBirthDateByMonth(3, dayInt)
            4 -> returnValue = checkBirthDateByMonth(2, dayInt)
            5 -> returnValue = checkBirthDateByMonth(3, dayInt)
            6 -> returnValue = checkBirthDateByMonth(2, dayInt)
            7 -> returnValue = checkBirthDateByMonth(3, dayInt)
            8 -> returnValue = checkBirthDateByMonth(3, dayInt)
            9 -> returnValue = checkBirthDateByMonth(2, dayInt)
            10 -> returnValue = checkBirthDateByMonth(3, dayInt)
            11 -> returnValue = checkBirthDateByMonth(2, dayInt)
            12 -> returnValue = checkBirthDateByMonth(3, dayInt)
        }
        return returnValue
    }

    private fun checkBirthDateByMonth(type: Int, dayInt: Int): Boolean {
        var returnValue = false
        when (type) {
            1 -> if (dayInt in 1..28) {
                returnValue = true
            }

            2 -> if (dayInt in 1..30) {
                returnValue = true
            }

            3 -> if (dayInt in 1..31) {
                returnValue = true
            }
        }
        return returnValue
    }
}