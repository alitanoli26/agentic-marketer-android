package com.example.agenticmarketer.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val auth = FirebaseAuth.getInstance()

    private var oobCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnResetPassword.setOnClickListener { confirmReset() }
        binding.btnRequestNewLink.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
            finish()
        }

        oobCode = extractOobCode(intent?.data)

        if (oobCode.isNullOrEmpty()) {
            showInvalidLink()
        } else {
            verifyCode(oobCode!!)
        }
    }

    /** Pulls the "oobCode" query parameter out of the Firebase auth action link. */
    private fun extractOobCode(uri: Uri?): String? {
        if (uri == null) return null
        val mode = uri.getQueryParameter("mode")
        val code = uri.getQueryParameter("oobCode")
        return if (mode == "resetPassword" && !code.isNullOrEmpty()) code else code
    }

    private fun verifyCode(code: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvSubtitle.text = "Verifying reset link..."

        auth.verifyPasswordResetCode(code)
            .addOnSuccessListener { email ->
                binding.progressBar.visibility = View.GONE
                binding.tvSubtitle.text = "Enter a new password for $email"
                binding.layoutForm.visibility = View.VISIBLE
                binding.layoutInvalidLink.visibility = View.GONE
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                showInvalidLink()
            }
    }

    private fun showInvalidLink() {
        binding.progressBar.visibility = View.GONE
        binding.tvSubtitle.text = "We couldn't verify this reset link."
        binding.layoutForm.visibility = View.GONE
        binding.layoutInvalidLink.visibility = View.VISIBLE
    }

    private fun confirmReset() {
        val code = oobCode ?: return showInvalidLink()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnResetPassword.isEnabled = false

        auth.confirmPasswordReset(code, password)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Password updated! Please log in.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                binding.btnResetPassword.isEnabled = true
                val message = if (exception is FirebaseAuthActionCodeException) {
                    "This link has expired or already been used."
                } else {
                    exception.message ?: "Something went wrong."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
    }
}
