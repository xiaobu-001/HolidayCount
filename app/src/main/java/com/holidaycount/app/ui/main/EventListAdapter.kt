package com.holidaycount.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.holidaycount.app.R
import com.holidaycount.app.data.model.EventItem
import com.holidaycount.app.databinding.ItemEventBinding
import com.holidaycount.app.utils.DateCalculation

class EventListAdapter(
    private val onItemClick: (EventItem) -> Unit
) : ListAdapter<EventItem, EventListAdapter.EventViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EventItem>() {
            override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean =
                oldItem == newItem
        }
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: EventItem) {
            binding.tvEmoji.text = event.emoji
            binding.tvEventName.text = event.name
            binding.tvDate.text = DateCalculation.formatDate(event.targetDate)

            binding.tvDaysLeft.text = when {
                event.daysLeft == 0 -> "今天！"
                event.daysLeft > 0 -> "${event.daysLeft}"
                else -> "${-event.daysLeft}"
            }

            binding.tvDaysUnit.text = when {
                event.daysLeft == 0 -> ""
                event.daysLeft > 0 -> "天后"
                else -> "天前"
            }

            // 背景颜色
            binding.cardBackground.setCardBackgroundColor(event.backgroundColor)

            // 标签（仅自定义事件可点击）
            if (event.id > 0) {
                binding.root.setOnClickListener { onItemClick(event) }
                binding.root.isClickable = true
            } else {
                binding.root.isClickable = false
            }

            // 今天特殊样式
            if (event.isToday) {
                binding.tvDaysLeft.textSize = 28f
            } else {
                binding.tvDaysLeft.textSize = 24f
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
