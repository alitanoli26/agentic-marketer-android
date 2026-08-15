package com.example.agenticmarketer.ui.fragments

import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.agenticmarketer.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.agenticmarketer.databinding.FragmentHistoryBinding
import com.example.agenticmarketer.models.HistoryItem
import com.example.agenticmarketer.ui.adapters.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: HistoryAdapter
    private val allItems = mutableListOf<HistoryItem>()
    private var currentFilter = "all"
    private var searchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        setupSearch()
        loadHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            items = emptyList(),
            onItemClick = { item -> showContentDialog(item) },
            onDeleteClick = { item, position -> confirmDelete(item, position) }
        )
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupFilters() {
        val chips = listOf(
            binding.chipAll to "all",
            binding.chipBlogs to "blog",
            binding.chipCaptions to "caption",
            binding.chipHashtags to "hashtags",
            binding.chipImages to "image"
        )

        chips.forEach { (chip, type) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    currentFilter = type
                    applyFilterAndSearch()
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString().trim().lowercase()
                applyFilterAndSearch()
            }
        })
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            showEmptyState()
            return
        }

        binding.layoutEmpty.visibility = View.GONE

        firestore.collection("generated_content")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                // Fragment may have been swapped out (fast tab switching) while this
                // network call was in flight — bail out instead of touching a dead view.
                if (_binding == null) return@addOnSuccessListener

                allItems.clear()
                allItems.addAll(
                    snapshot.documents.mapNotNull { doc ->
                        val data = doc.data
                        if (data != null) {
                            HistoryItem(
                                id = doc.id,
                                userId = data["userId"] as? String ?: "",
                                topic = data["topic"] as? String ?: "",
                                content = data["content"] as? String ?: "",
                                type = data["type"] as? String ?: "blog",
                                timestamp = data["timestamp"] as? Long ?: 0,
                                imageUrl = data["imageUrl"] as? String ?: ""
                            )
                        } else null
                    }
                )

                updateStats()
                applyFilterAndSearch()

                if (allItems.isEmpty()) {
                    showEmptyState()
                }
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                Toast.makeText(requireContext(), "Error loading history: ${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState()
            }
    }

    private fun applyFilterAndSearch() {
        // ✅ FIX 1: Explicit type declaration
        var filtered: List<HistoryItem> = allItems

        // Apply filter
        if (currentFilter != "all") {
            filtered = filtered.filter { it.type == currentFilter }
        }

        // Apply search
        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.topic.lowercase().contains(searchQuery) ||
                        it.content.lowercase().contains(searchQuery)
            }
        }

        // Update count
        binding.tvTotalCount.text = "${filtered.size} items"

        if (filtered.isEmpty()) {
            binding.rvHistory.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
            
            // ✅ FIX 2: TextView reference sahi karein
            val emptyMessage = binding.layoutEmpty.findViewById<TextView>(R.id.tvEmptyMessage)
            emptyMessage?.text = if (allItems.isEmpty()) "No content generated yet" else "No results found"
        } else {
            binding.rvHistory.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            adapter.updateItems(filtered)
        }
    }

    private fun updateStats() {
        val blogs = allItems.count { it.type == "blog" }
        val captions = allItems.count { it.type == "caption" }
        val hashtags = allItems.count { it.type == "hashtags" }
        val images = allItems.count { it.type == "image" }

        binding.tvStatBlogs.text = "$blogs Blogs"
        binding.tvStatCaptions.text = "$captions Captions"
        binding.tvStatHashtags.text = "$hashtags Hashtags"
        binding.tvStatImages.text = "$images Images"
    }

    private fun showEmptyState() {
        binding.rvHistory.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.tvTotalCount.text = "0 items"
        updateStats()
    }

    private fun showContentDialog(item: HistoryItem) {
        if (item.type == "image" && item.imageUrl.isNotEmpty()) {
            showImagePreviewDialog(item)
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(item.topic.ifEmpty { item.typeLabel })
                .setMessage(item.content)
                .setPositiveButton("Copy") { _, _ ->
                    copyToClipboard(item.content)
                }
                .setNegativeButton("Close", null)
                .show()
        }
    }

    private fun showImagePreviewDialog(item: HistoryItem) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_image_preview, null)

        val ivPreview = dialogView.findViewById<ImageView>(R.id.ivDialogImage)
        val tvPrompt = dialogView.findViewById<TextView>(R.id.tvDialogPrompt)
        val btnDownload = dialogView.findViewById<Button>(R.id.btnDialogDownload)

        tvPrompt.text = item.topic.ifEmpty { item.content }

        var loadedBitmap: Bitmap? = null

        Glide.with(this)
            .asBitmap()
            .load(item.imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    loadedBitmap = resource
                    ivPreview.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}

                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    Toast.makeText(requireContext(), "Could not load image", Toast.LENGTH_SHORT).show()
                }
            })

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(item.typeLabel)
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .create()

        btnDownload.setOnClickListener {
            val bitmap = loadedBitmap
            if (bitmap == null) {
                Toast.makeText(requireContext(), "Image still loading, try again", Toast.LENGTH_SHORT).show()
            } else {
                downloadBitmapToGallery(bitmap)
            }
        }

        dialog.show()
    }

    private fun downloadBitmapToGallery(bitmap: Bitmap) {
        try {
            val filename = "AgenticMarketer_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgenticMarketer")
                }
            }

            val uri = requireContext().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            )
            if (uri != null) {
                requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                Toast.makeText(requireContext(), "Image saved to gallery!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Could not save image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("History Content", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete(item: HistoryItem, position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this ${item.typeLabel}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteItem(item, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(item: HistoryItem, position: Int) {
        firestore.collection("generated_content")
            .document(item.id)
            .delete()
            .addOnSuccessListener {
                if (_binding == null) return@addOnSuccessListener
                allItems.removeAt(position)
                updateStats()
                applyFilterAndSearch()
                Toast.makeText(requireContext(), "Deleted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                Toast.makeText(requireContext(), "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}