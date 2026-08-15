package com.example.agenticmarketer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.agenticmarketer.databinding.FragmentProfileBinding
import com.example.agenticmarketer.ui.activities.LoginActivity
import com.example.agenticmarketer.utils.UserPrefs
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        
        // ✅ Get Name
        val name = UserPrefs.getName(context)               // 1) Cached name
            ?: auth.currentUser?.displayName?.takeIf { it.isNotBlank() }?.also {
                UserPrefs.saveName(context, it)
            }
            ?: auth.currentUser?.email?.substringBefore("@") // 3) Email fallback
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: "Guest User"

        binding.tvUserName.text = name
        binding.tvUserEmail.text = auth.currentUser?.email ?: "Guest User"

        // ✅ WhatsApp Style Initials
        val email = auth.currentUser?.email ?: "user@example.com"
        val namePart = email.substringBefore("@")
        val initials = namePart.split(".")
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .take(2)
            .uppercase()
        binding.tvInitials.text = if (initials.isNotEmpty()) initials else "U"

        // ✅ Logout
        binding.btnLogout.setOnClickListener {
            UserPrefs.clear(context) // Clear cache
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}