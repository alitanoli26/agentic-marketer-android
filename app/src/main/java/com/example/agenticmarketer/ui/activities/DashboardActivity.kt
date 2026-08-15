package com.example.agenticmarketer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.agenticmarketer.R
import com.example.agenticmarketer.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
            val navController = navHostFragment.navController
            binding.bottomNav.setupWithNavController(navController)
            Log.d("Dashboard", "✅ Bottom navigation setup successful!")
        } catch (e: Exception) {
            Log.e("Dashboard", "❌ Bottom navigation setup failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}