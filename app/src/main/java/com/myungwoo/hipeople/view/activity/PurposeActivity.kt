package com.myungwoo.hipeople.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.databinding.ActivityPurposeBinding

class PurposeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurposeBinding
    private lateinit var purpose: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurposeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val gender = intent.getStringExtra("gender")

        binding.btnFriend.setOnClickListener {
            purpose = "친구 찾기"
            binding.btnFriend.setBackgroundColor(getColor(R.color.btncilck))
            binding.btnCasual.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnLove.setBackgroundColor(getColor(R.color.btncolor))
        }
        binding.btnCasual.setOnClickListener {
            purpose = "캐쥬얼한 관계 찾기"
            binding.btnFriend.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnCasual.setBackgroundColor(getColor(R.color.btncilck))
            binding.btnLove.setBackgroundColor(getColor(R.color.btncolor))
        }

        binding.btnLove.setOnClickListener {
            purpose = "인연 찾기"
            binding.btnFriend.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnCasual.setBackgroundColor(getColor(R.color.btncolor))
            binding.btnLove.setBackgroundColor(getColor(R.color.btncilck))
        }

        binding.btnNext3.setOnClickListener {
            val intent = Intent(this, BirthActivity::class.java)
            intent.putExtra("id", id)
            intent.putExtra("pw", pw)
            intent.putExtra("gender", gender)
            intent.putExtra("purpose", purpose)
            startActivity(intent)
        }
    }
}