package com.example.agenticmarketer.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivityRegisterBinding
import com.example.agenticmarketer.utils.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish() // back to LoginActivity
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    setLoading(false)
                    Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                // 1) Save the real name on the Firebase Auth profile itself
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user.updateProfile(profileUpdate)
                    .addOnCompleteListener {
                        // 2) Save the same name in Firestore (so it survives reinstalls / new devices)
                        val userDoc = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )
                        firestore.collection("users").document(user.uid)
                            .set(userDoc)
                            .addOnCompleteListener {
                                // 3) Cache the name locally so Home/Profile can show it instantly
                                UserPrefs.saveName(this, name)
                                UserPrefs.saveEmail(this, email)

                                setLoading(false)
                                startDashboard()
                            }
                    }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }

    private fun startDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
