package com.example.agenticmarketer.ui.activities

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.content.FileProvider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.agenticmarketer.api.RetrofitClient
import com.example.agenticmarketer.databinding.ActivityImageGeneratorBinding
import com.example.agenticmarketer.models.HuggingFaceImageRequest
import com.example.agenticmarketer.utils.Config
import com.example.agenticmarketer.utils.ImageQuotaTracker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class ImageGeneratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageGeneratorBinding
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var currentPrompt: String = ""
    private var currentBitmap: Bitmap? = null

    // FLUX.1-schnell via Hugging Face's free "hf-inference" provider: both
    // Google Gemini image models (2.5 and 3.1) hit a "limit: 0" free-tier
    // bug on this Google Cloud account (confirmed via Logcat — Google's own
    // bug, not fixable from app code), so we switched to Hugging Face's
    // serverless free tier instead. Schnell is the fast, free-tier-friendly
    // FLUX variant — good quality, no Google account issues.
    private val imageModel = "black-forest-labs/FLUX.1-schnell"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val topic = intent.getStringExtra("topic")
        if (topic != null) {
            binding.etPrompt.setText(topic)
        }

        setupUI()
        updateQuotaUi()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnGenerate.setOnClickListener {
            generateImage()
        }

        binding.btnDownload.setOnClickListener {
            downloadImage()
        }

        binding.btnSaveToHistory.setOnClickListener {
            saveImageToHistory()
        }

        binding.btnSchedule.setOnClickListener {
            // Navigate to Scheduler
            Toast.makeText(this, "Scheduler coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateQuotaUi() {
        val limit = Config.IMAGE_DAILY_LIMIT
        val used = ImageQuotaTracker.getUsedToday(this)
        val remaining = (limit - used).coerceAtLeast(0)

        binding.tvQuotaLabel.text = "Free images today: $used / $limit"
        val percentUsed = if (limit > 0) ((used.toFloat() / limit) * 100).toInt().coerceIn(0, 100) else 100
        binding.progressQuota.progress = percentUsed

        val outOfQuota = remaining <= 0
        binding.btnGenerate.isEnabled = !outOfQuota
        if (outOfQuota) {
            binding.tvQuotaLabel.text = "Free images today: $used / $limit — limit reached, try again tomorrow"
        }
    }

    private fun generateImage() {
        val userPrompt = binding.etPrompt.text.toString().trim()

        if (userPrompt.isEmpty()) {
            Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ImageQuotaTracker.hasQuotaLeft(this, Config.IMAGE_DAILY_LIMIT)) {
            Toast.makeText(
                this,
                "You've used today's free image quota. Try again tomorrow.",
                Toast.LENGTH_LONG
            ).show()
            updateQuotaUi()
            return
        }

        currentPrompt = userPrompt
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGenerate.isEnabled = false

        // Pollinations only ever saw the raw prompt and had no real
        // understanding of it, which is why "a car" came back as a flat
        // cartoon car. FLUX.1-schnell is a real diffusion model, so we wrap
        // the user's prompt with light marketing-image guidance to push it
        // toward clean, professional, on-brand output instead of generic
        // stock-art results — without overriding whatever style they asked for.
        val enhancedPrompt = """
            Professional marketing photograph: $userPrompt.
            High-quality, photorealistic, clean composition, good lighting,
            no watermarks, no text overlays.
        """.trimIndent()

        lifecycleScope.launch {
            try {
                val request = HuggingFaceImageRequest(inputs = enhancedPrompt)

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.huggingFaceApi.generateImage(
                        model = imageModel,
                        auth = "Bearer ${Config.HUGGINGFACE_API_KEY}",
                        request = request
                    )
                }

                // We count this as "used" even if it later turns out to have
                // failed for a non-quota reason, since the request still hit
                // the API. The only case we deliberately don't count is the
                // early-return above when we already know the local quota is
                // exhausted — that one never touches the network.
                ImageQuotaTracker.recordUsage(this@ImageGeneratorActivity)
                updateQuotaUi()

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ImageGenerator", "API error ${response.code()}: $errorBody")
                    onGenerationFailed(
                        when (response.code()) {
                            401, 403 -> "Invalid Hugging Face token. Check Config.kt."
                            402 -> "Hugging Face free credits exhausted for this month."
                            429 -> "Hugging Face free quota/rate limit hit. Try again shortly."
                            503 -> "Model is warming up (cold start). Wait ~30s and try again."
                            else -> "Image generation failed (${response.code()})."
                        }
                    )
                    return@launch
                }

                // Unlike OpenRouter/Gemini, Hugging Face's response body IS the
                // image — raw PNG/JPEG bytes, not JSON — so we read it directly.
                val imageBytes = withContext(Dispatchers.IO) {
                    response.body()?.bytes()
                }

                if (imageBytes == null || imageBytes.isEmpty()) {
                    Log.e("ImageGenerator", "Empty image response body")
                    onGenerationFailed("Model returned no image. Try rephrasing the prompt.")
                    return@launch
                }

                val bitmap = withContext(Dispatchers.Default) {
                    decodeBytesToBitmap(imageBytes)
                }
                if (bitmap == null) {
                    onGenerationFailed("Could not decode the generated image.")
                    return@launch
                }

                currentBitmap = bitmap
                binding.ivResult.setImageBitmap(bitmap)
                binding.progressBar.visibility = View.GONE
                binding.cardResult.visibility = View.VISIBLE
                binding.btnSchedule.visibility = View.VISIBLE
                // Re-enable Generate only if quota remains for another try.
                binding.btnGenerate.isEnabled =
                    ImageQuotaTracker.hasQuotaLeft(this@ImageGeneratorActivity, Config.IMAGE_DAILY_LIMIT)
            } catch (e: Exception) {
                Log.e("ImageGenerator", "Exception during image generation", e)
                onGenerationFailed("Network error: ${e.message ?: "could not reach server"}")
            }
        }
    }

    private fun onGenerationFailed(message: String) {
        binding.progressBar.visibility = View.GONE
        // Don't re-enable the button if the local quota is already exhausted.
        binding.btnGenerate.isEnabled =
            ImageQuotaTracker.hasQuotaLeft(this, Config.IMAGE_DAILY_LIMIT)
        Toast.makeText(this@ImageGeneratorActivity, message, Toast.LENGTH_LONG).show()
    }

    private fun decodeBytesToBitmap(bytes: ByteArray): Bitmap? {
        return try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e("ImageGenerator", "Failed to decode image bytes", e)
            null
        }
    }

    private fun downloadImage() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            Toast.makeText(this, "No image to download yet", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val filename = "AgenticMarketer_${System.currentTimeMillis()}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/AgenticMarketer")
                }
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                Toast.makeText(this, "Image saved to gallery!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Could not save image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImage() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Generate an image first", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val sharedImagesDir = File(cacheDir, "shared_images").apply { mkdirs() }
            val file = File(sharedImagesDir, "share_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share image via")

            // The Chooser UI itself (and every app it lists) needs an explicit
            // grant — FLAG_GRANT_READ_URI_PERMISSION on the inner intent alone
            // isn't always enough for the system Chooser to read the file for
            // its preview, which causes a SecurityException/crash in some
            // OEM ROMs (e.g. Transsion/Infinix/Tecno's "android:ui" chooser).
            val resInfoList = packageManager.queryIntentActivities(shareIntent, 0)
            for (resolveInfo in resInfoList) {
                val targetPackage = resolveInfo.activityInfo.packageName
                grantUriPermission(targetPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToHistory() {
        val bitmap = currentBitmap
        if (bitmap == null) {
            Toast.makeText(this, "Generate an image first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSaveToHistory.isEnabled = false

        // Firebase Storage requires the Blaze (pay-as-you-go) plan, which needs
        // a card on file even though usage stays within the free tier — we're
        // avoiding that requirement, so we save a compressed thumbnail
        // directly in Firestore as a base64 data URI in the same "imageUrl"
        // field the History screen already reads. Glide (used by
        // HistoryFragment/HistoryAdapter) loads data URIs natively, so no
        // changes are needed there. Firestore's per-document limit is 1MB,
        // so we resize down and compress hard to comfortably fit.
        val maxDimension = 512
        val scale = minOf(
            maxDimension.toFloat() / bitmap.width,
            maxDimension.toFloat() / bitmap.height,
            1f // never upscale
        )
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val baos = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 60, baos)
        val imageBytes = baos.toByteArray()
        val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val dataUri = "data:image/jpeg;base64,$base64Image"

        Log.d("ImageGenerator", "History thumbnail size: ${imageBytes.size / 1024}KB")

        val userId = auth.currentUser?.uid ?: "anonymous"
        val data = hashMapOf(
            "userId" to userId,
            "topic" to currentPrompt,
            "content" to currentPrompt,
            "type" to "image",
            "imageUrl" to dataUri,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("generated_content")
            .add(data)
            .addOnSuccessListener {
                binding.btnSaveToHistory.isEnabled = true
                Toast.makeText(this, "Saved to history!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                binding.btnSaveToHistory.isEnabled = true
                Log.e("ImageGenerator", "Firestore save failed", e)
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}