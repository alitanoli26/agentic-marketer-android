package com.example.agenticmarketer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivityLoginBinding
import com.example.agenticmarketer.utils.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser != null) {
            startDashboard()
        }

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    cacheUserNameThenContinue()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Makes sure the real name is cached locally before going to the dashboard,
     * so Home/Profile can show it instantly on this device too.
     */
    private fun cacheUserNameThenContinue() {
        val user = auth.currentUser
        if (user == null) {
            startDashboard()
            return
        }

        // Already cached on this device? Just go.
        if (UserPrefs.getName(this) != null) {
            startDashboard()
            return
        }

        // Try the name stored on the Firebase Auth profile first (fast, no extra read needed).
        val authName = user.displayName
        if (!authName.isNullOrBlank()) {
            UserPrefs.saveName(this, authName)
            user.email?.let { UserPrefs.saveEmail(this, it) }
            startDashboard()
            return
        }

        // Fallback: pull it from Firestore (covers accounts created before this field existed).
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name")
                if (!name.isNullOrBlank()) {
                    UserPrefs.saveName(this, name)
                }
                user.email?.let { UserPrefs.saveEmail(this, it) }
                startDashboard()
            }
            .addOnFailureListener {
                startDashboard()
            }
    }

    private fun startDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}