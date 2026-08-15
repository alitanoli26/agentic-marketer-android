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
import com.example.agenticmarketer.databinding.ActivityBlogGeneratorBinding
import com.example.agenticmarketer.viewmodels.AIViewModel

class BlogGeneratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogGeneratorBinding
    private val viewModel: AIViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val tones = arrayOf("Professional", "Casual", "Friendly", "Marketing")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tones)
        binding.spinnerTone.setAdapter(adapter)

        binding.btnGenerate.setOnClickListener {
            val topic = binding.etTopic.text.toString().trim()
            val tone = binding.spinnerTone.text.toString()
            if (topic.isNotEmpty()) {
                viewModel.generateContent("blog", topic, tone)
            } else {
                Toast.makeText(this, "Please enter a topic", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCopy.setOnClickListener {
            copyToClipboard(binding.tvResult.text.toString())
        }

        binding.btnShare.setOnClickListener {
            shareContent(binding.tvResult.text.toString())
        }

        binding.btnHumanize.setOnClickListener {
            val currentContent = binding.tvResult.text.toString()
            if (currentContent.isNotEmpty()) {
                binding.btnHumanize.isEnabled = false
                viewModel.generateContent("humanize", currentContent)
            } else {
                Toast.makeText(this, "Generate content first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AIViewModel.AIUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnGenerate.isEnabled = false
                    binding.btnHumanize.isEnabled = false
                }
                is AIViewModel.AIUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerate.isEnabled = true
                    binding.btnHumanize.isEnabled = true
                    binding.cardResult.visibility = View.VISIBLE
                    binding.tvResult.text = state.content
                }
                is AIViewModel.AIUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnGenerate.isEnabled = true
                    binding.btnHumanize.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Blog Content", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareContent(text: String) {
        if (text.isBlank()) {
            Toast.makeText(this, "Nothing to share. Generate content first!", Toast.LENGTH_SHORT).show()
            return
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}