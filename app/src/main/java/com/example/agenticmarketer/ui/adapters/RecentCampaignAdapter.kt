package com.example.agenticmarketer.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.agenticmarketer.R
import com.example.agenticmarketer.models.HistoryItem
import com.google.android.material.card.MaterialCardView

/**
 * Lightweight adapter for the Home screen "Recent Campaigns" strip.
 * Shows at most 5 items so the home page stays snappy.
 */
class RecentCampaignAdapter(
    private var items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<RecentCampaignAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val cvTypeIcon: MaterialCardView = view.findViewById(R.id.cvTypeIcon)
        val tvTypeIcon: TextView         = view.findViewById(R.id.tvTypeIcon)
        val tvTopic: TextView            = view.findViewById(R.id.tvCampaignTopic)
        val tvType: TextView             = view.findViewById(R.id.tvCampaignType)
        val tvTime: TextView             = view.findViewById(R.id.tvCampaignTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_campaign, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // ── Emoji icon + bg tint per type ──
        val (emoji, bgColor) = when (item.type) {
            "blog"     -> "📝" to 0xFFEEF0FF.toInt()
            "caption"  -> "📱" to 0xFFFFF0F5.toInt()
            "hashtags" -> "#️⃣" to 0xFFF5EEFF.toInt()
            "image"    -> "🖼️" to 0xFFFFF8EE.toInt()
            else       -> "📄" to 0xFFEEEEFF.toInt()
        }
        holder.tvTypeIcon.text = emoji
        holder.cvTypeIcon.setCardBackgroundColor(bgColor)

        // ── Type badge background ──
        val badgeDrawable = when (item.type) {
            "blog"     -> R.drawable.bg_type_blog
            "caption"  -> R.drawable.bg_type_caption
            "hashtags" -> R.drawable.bg_type_hashtag
            "image"    -> R.drawable.bg_type_image
            else       -> R.drawable.bg_type_blog
        }
        holder.tvType.setBackgroundResource(badgeDrawable)
        holder.tvType.text = item.typeLabel

        holder.tvTopic.text = item.topic.ifBlank { "Untitled" }
        holder.tvTime.text  = item.formattedDate

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    /** Smooth diff-based update — no flicker */
    fun submitList(newItems: List<HistoryItem>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(o: Int, n: Int) = items[o].id == newItems[n].id
            override fun areContentsTheSame(o: Int, n: Int) = items[o] == newItems[n]
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }
}