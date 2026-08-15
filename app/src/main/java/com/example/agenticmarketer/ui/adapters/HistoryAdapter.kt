package com.example.agenticmarketer.ui.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agenticmarketer.R
import com.example.agenticmarketer.models.HistoryItem

class HistoryAdapter(
    private var items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryItem, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTopic: TextView = itemView.findViewById(R.id.tvTopic)
        private val tvPreview: TextView = itemView.findViewById(R.id.tvPreview)
        private val tvTypeBadge: TextView = itemView.findViewById(R.id.tvTypeBadge)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val ivImagePreview: ImageView = itemView.findViewById(R.id.ivImagePreview)
        private val btnCopy: ImageButton = itemView.findViewById(R.id.btnCopy)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: HistoryItem) {
            tvTopic.text = item.topic.ifEmpty { "Untitled" }
            tvPreview.text = item.previewContent
            tvTypeBadge.text = item.typeLabel
            tvTimestamp.text = item.formattedDate

            // Type badge color
            val bgRes = when (item.type) {
                "blog" -> R.drawable.bg_type_blog
                "caption" -> R.drawable.bg_type_caption
                "hashtags" -> R.drawable.bg_type_hashtag
                "image" -> R.drawable.bg_type_image
                else -> R.drawable.bg_type_blog
            }
            tvTypeBadge.setBackgroundResource(bgRes)

            // Image preview
            if (item.type == "image" && item.imageUrl.isNotEmpty()) {
                ivImagePreview.visibility = View.VISIBLE
                Glide.with(ivImagePreview.context)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivImagePreview)
            } else {
                ivImagePreview.visibility = View.GONE
            }

            // ✅ Click listener on whole item
            itemView.setOnClickListener {
                onItemClick(item)
            }

            // Copy
            btnCopy.setOnClickListener {
                copyToClipboard(itemView.context, item.content)
            }

            // Delete
            btnDelete.setOnClickListener {
                onDeleteClick(item, adapterPosition)
            }
        }

        private fun copyToClipboard(context: Context, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("History Content", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }
}