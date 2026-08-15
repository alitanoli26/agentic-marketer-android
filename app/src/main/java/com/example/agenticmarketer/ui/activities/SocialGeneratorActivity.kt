package com.example.agenticmarketer.ui.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.agenticmarketer.databinding.ActivitySocialGeneratorBinding
import com.example.agenticmarketer.viewmodels.AIViewModel

class SocialGeneratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySocialGeneratorBinding
    private val viewModel: AIViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val platforms = arrayOf("Facebook", "Instagram", "LinkedIn", "Twitter/X")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, platforms)
        binding.spinnerPlatform.setAdapter(adapter)

        binding.btnGenerate.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            val platform = binding.spinnerPlatform.text.toString()
            if (topic.isNotEmpty()) {
                viewModel.generateContent("caption", topic, platform)
            } else {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCopy.setOnClickListener {
            copyToClipboard(binding.tvResult.text.toString())
        }

        binding.btnShare.setOnClickListener {
            val text = binding.tvResult.text.toString()
            if (text.isBlank()) {
                Toast.makeText(this, "Nothing to share. Generate content first!", Toast.LENGTH_SHORT).show()
            } else {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }

        binding.btnNext.setOnClickListener {
            val intent = Intent(this, ImageGeneratorActivity::class.java).apply {
                putExtra("topic", binding.etTopic.text.toString())
                putExtra("caption", binding.tvResult.text.toString())
                putExtra("platform", binding.spinnerPlatform.text.toString())
            }
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AIViewModel.AIUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnGenerate.isEnabled = false
                }
                is AIViewModel.AIUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerate.isEnabled = true
                    binding.cardResult.visibility = View.VISIBLE
                    binding.tvResult.text = state.content
                }
                is AIViewModel.AIUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerate.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Social Caption", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}