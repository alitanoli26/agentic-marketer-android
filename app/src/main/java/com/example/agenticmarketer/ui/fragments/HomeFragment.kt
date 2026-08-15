package com.example.agenticmarketer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.agenticmarketer.R
import com.example.agenticmarketer.databinding.FragmentHomeBinding
import com.example.agenticmarketer.ui.activities.BlogGeneratorActivity
import com.example.agenticmarketer.ui.activities.ImageGeneratorActivity
import com.example.agenticmarketer.ui.activities.SocialGeneratorActivity
import com.example.agenticmarketer.utils.UserPrefs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ── In-memory cache (lives as long as the Fragment instance) ──────────────
    private var cachedBlogCount    = -1
    private var cachedPostCount    = -1
    private var cachedImageCount   = -1

    // Pro-tip rotation
    private val tips = listOf(
        "Add your brand tone in the topic for better results! 🎯",
        "Describe your target audience to get more relevant captions 👥",
        "Use specific product names — the AI picks up on details 🔍",
        "Generate a blog post, then humanise it for a polished finish ✨",
        "Try 'casual and witty' as a tone for Instagram captions 😄"
    )

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTip()
        setupGreeting()

        if (cachedBlogCount >= 0) {
            showStats(cachedBlogCount, cachedPostCount, cachedImageCount)
            refreshStatsInBackground()
        } else {
            loadStats(auth.currentUser?.uid)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Setup ──────────────────────────────────────────────────────────────────

    private fun setupClickListeners() {
        binding.cardAiBlog.setOnClickListener {
            startActivity(Intent(requireContext(), BlogGeneratorActivity::class.java))
        }
        binding.cardSocialPost.setOnClickListener {
            startActivity(Intent(requireContext(), SocialGeneratorActivity::class.java))
        }
        binding.cardAiImage.setOnClickListener {
            startActivity(Intent(requireContext(), ImageGeneratorActivity::class.java))
        }
        binding.cardHistory.setOnClickListener {
            findNavController().navigate(R.id.nav_history)
        }
    }

    private fun setupTip() {
        binding.tvTipText.text = tips.random()
    }

    private fun setupGreeting() {
        val context = requireContext()
        val name = UserPrefs.getName(context)              // 1) cached real name (instant, set at signup)
            ?: auth.currentUser?.displayName?.takeIf { it.isNotBlank() }?.also {
                UserPrefs.saveName(context, it)             // 2) Auth profile name — cache it for next time
            }
            ?: auth.currentUser?.email?.substringBefore("@") // 3) last-resort fallback
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: "Marketer"
        binding.tvUserName.text = name

        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when (hour) {
            in 5..11  -> "Good Morning 👋"
            in 12..16 -> "Good Afternoon 👋"
            else       -> "Good Evening 👋"
        }
    }

    // ── Loading states ─────────────────────────────────────────────────────────

    private fun showStats(blogs: Int, posts: Int, images: Int) {
        binding.tvBlogsCount.text  = blogs.toString()
        binding.tvPostsCount.text  = posts.toString()
        binding.tvImagesCount.text = images.toString()
    }

    private fun refreshStatsInBackground() {
        loadStats(auth.currentUser?.uid, silent = true)
    }

    private fun loadStats(userId: String?, silent: Boolean = false) {
        if (userId == null) return

        var blogs  = 0
        var posts  = 0
        var images = 0
        var done   = 0

        fun checkDone() {
            done++
            if (done == 3 && _binding != null) {
                cachedBlogCount  = blogs
                cachedPostCount  = posts
                cachedImageCount = images
                showStats(blogs, posts, images)
            }
        }

        firestore.collection("generated_content")
            .whereEqualTo("userId", userId).whereEqualTo("type", "blog")
            .get().addOnSuccessListener  { blogs  = it.size(); checkDone() }
            .addOnFailureListener        { checkDone() }

        firestore.collection("generated_content")
            .whereEqualTo("userId", userId).whereEqualTo("type", "caption")
            .get().addOnSuccessListener  { posts  = it.size(); checkDone() }
            .addOnFailureListener        { checkDone() }

        firestore.collection("generated_content")
            .whereEqualTo("userId", userId).whereEqualTo("type", "image")
            .get().addOnSuccessListener  { images = it.size(); checkDone() }
            .addOnFailureListener        { checkDone() }
    }
}
