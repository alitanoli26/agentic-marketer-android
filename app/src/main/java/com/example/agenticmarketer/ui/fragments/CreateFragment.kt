package com.example.agenticmarketer.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.agenticmarketer.databinding.FragmentCreateBinding
import com.example.agenticmarketer.ui.activities.ImageGeneratorActivity
import com.example.agenticmarketer.viewmodels.AIViewModel

class CreateFragment : Fragment() {
    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AIViewModel by viewModels()
    private var currentType = "blog"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Tone Spinner (for Blog)
        val tones = arrayOf("Professional", "Casual", "Friendly", "Marketing")
        binding.spinnerTone.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                tones
            )
        )

        // Platform Spinner (for Caption)
        val platforms = arrayOf("Instagram", "Facebook", "LinkedIn", "Twitter")
        binding.spinnerPlatform.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                platforms
            )
        )

        // ✅ BUTTON 1: Generate Blog
        binding.btnGenerateBlog.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            val tone = binding.spinnerTone.text.toString()
            if (topic.isNotEmpty()) {
                currentType = "blog"
                viewModel.generateContent("blog", topic, tone)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a topic",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // ✅ BUTTON 2: Generate Caption
        binding.btnGenerateCaption.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            val platform = binding.spinnerPlatform.text.toString()
            if (topic.isNotEmpty()) {
                currentType = "caption"
                viewModel.generateContent("caption", topic, platform)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a topic",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // ✅ BUTTON 3: Generate Image — OPENS ImageGeneratorActivity
        binding.btnGenerateImage.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            if (topic.isNotEmpty()) {
                val intent = Intent(requireContext(), ImageGeneratorActivity::class.java).apply {
                    putExtra("topic", topic)
                }
                startActivity(intent)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a topic for image generation",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // ✅ BUTTON 4: Generate Hashtags — NEW BUTTON
        binding.btnGenerateHashtags.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            if (topic.isNotEmpty()) {
                currentType = "hashtags"
                viewModel.generateContent("hashtags", topic)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter a topic for hashtags",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Copy Button
        binding.btnCopy.setOnClickListener {
            val content = binding.tvGeneratedCaption.text.toString()
            if (content.isNotEmpty()) {
                val clipboard =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AI Content", content)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    requireContext(),
                    "Copied to clipboard",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Nothing to copy. Generate content first!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Share Button
        binding.btnShare.setOnClickListener {
            val content = binding.tvGeneratedCaption.text.toString()
            if (content.isNotEmpty()) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                Toast.makeText(
                    requireContext(),
                    "Nothing to share. Generate content first!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Save Button
        binding.btnSave.setOnClickListener {
            val content = binding.tvGeneratedCaption.text.toString()
            val topic = binding.etTopic.text.toString()
            if (content.isNotEmpty()) {
                viewModel.saveContent(topic, content, currentType)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Nothing to save. Generate content first!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AIViewModel.AIUiState.Loading -> {
                    binding.cardLoading.visibility = View.VISIBLE
                    binding.cardResult.visibility = View.GONE
                }
                is AIViewModel.AIUiState.Success -> {
                    binding.cardLoading.visibility = View.GONE
                    binding.cardResult.visibility = View.VISIBLE
                    binding.tvGeneratedCaption.text = state.content
                }
                is AIViewModel.AIUiState.Error -> {
                    binding.cardLoading.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "Saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to save",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}