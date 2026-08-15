package com.example.agenticmarketer.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivitySignupBinding
import com.example.agenticmarketer.utils.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.tvLogin.setOnClickListener { finish() }

        binding.btnSignup.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user?.updateProfile(profileUpdates)

                val userData = hashMapOf(
                    "uid" to (user?.uid ?: ""),
                    "name" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )

                if (user != null) {
                    firestore.collection("users").document(user.uid)
                        .set(userData)
                        .addOnCompleteListener {
                            // Cache the real name locally now, while we have it,
                            // so Home/Profile can show it instantly after login.
                            UserPrefs.saveName(this, name)
                            UserPrefs.saveEmail(this, email)

                            setLoading(false)
                            goToLogin()
                        }
                } else {
                    setLoading(false)
                    goToLogin()
                }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !loading
    }

    private fun goToLogin() {
        // Firebase auto-signs-in the user after createUserWithEmailAndPassword,
        // but real apps require an explicit login — so sign them back out.
        auth.signOut()
        Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_LONG).show()
        val intent = android.content.Intent(this, LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}