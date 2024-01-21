package com.myungwoo.hipeople.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.myungwoo.hipeople.databinding.ActivitySplashActivtyBinding

class SplashActivty : AppCompatActivity() {

    private lateinit var binding: ActivitySplashActivtyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashActivtyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            Intent(this, MainActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }, 3000)
    }
}