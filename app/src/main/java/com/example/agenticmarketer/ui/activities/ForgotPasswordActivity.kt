package com.example.agenticmarketer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val auth = FirebaseAuth.getInstance()

    companion object {
        // Default Firebase Auth handler domain for this project — already
        // authorized by Firebase, so no extra console setup is required.
        private const val CONTINUE_URL = "https://agenticmarketer-684a1.firebaseapp.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvBackToLogin.setOnClickListener { finish() }

        binding.btnSendLink.setOnClickListener {
            sendResetLink()
        }
    }

    private fun sendResetLink() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(CONTINUE_URL)
            .setHandleCodeInApp(true)
            .setAndroidPackageName(packageName, false, null)
            .build()

        auth.sendPasswordResetEmail(email, actionCodeSettings)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(
                    this,
                    "Reset link sent! Check your inbox for $email",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSendLink.isEnabled = !loading
    }
}
