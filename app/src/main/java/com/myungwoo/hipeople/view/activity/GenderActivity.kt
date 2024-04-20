package com.myungwoo.hipeople.view.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.myungwoo.hipeople.R
import com.myungwoo.hipeople.databinding.ActivityGenderBinding

class GenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenderBinding
    private lateinit var gender: String
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addOnBackPressedCallback()
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

    private fun addOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@GenderActivity)
                    .setTitle(R.string.gender_alert_dialog_title)
                    .setMessage(R.string.gender_alert_dialog_message)
                    .setPositiveButton(
                        R.string.gender_alert_dialog_positive,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                handleOnBackCancelled()
                            }
                        })
                    .setNegativeButton(
                        R.string.gender_alert_dialog_negative,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface, which: Int) {
                                startActivity(Intent(this@GenderActivity, MainActivity::class.java))
                            }
                        })
                    .create()
                    .show()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }
}