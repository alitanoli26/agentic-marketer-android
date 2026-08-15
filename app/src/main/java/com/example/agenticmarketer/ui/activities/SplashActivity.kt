package com.example.agenticmarketer.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            // ✅ FIX: Check if user is logged in
            if (auth.currentUser != null) {
                // User is logged in → Go to Dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // User is NOT logged in → Go to Login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)  // Reduced to 2 seconds
    }
}