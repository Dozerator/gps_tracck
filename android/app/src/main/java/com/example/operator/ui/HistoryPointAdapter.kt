package com.example.operator.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.operator.R
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.databinding.ItemHistoryPointBinding
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryPointAdapter : ListAdapter<PendingPointEntity, HistoryPointAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryPointBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), timeFormat)
    }

    class ViewHolder(private val binding: ItemHistoryPointBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(point: PendingPointEntity, timeFormat: SimpleDateFormat) {
            val context = binding.root.context
            val objectType = ObjectType.entries.find { it.apiValue == point.objectType }
            val threatLevel = ThreatLevel.entries.find { it.apiValue == point.threatLevel } ?: ThreatLevel.OBSERVATION
            val icon = if (objectType == ObjectType.QUAD) "⬡" else "✈"

            binding.itemObjectLine.text = "$icon ${point.objectType}"
            binding.itemThreatBadge.text = "● ${threatLevel.label}"
            binding.itemThreatBadge.setTextColor(ContextCompat.getColor(context, threatColorRes(threatLevel)))
            binding.itemDirectionLine.text = context.getString(
                R.string.history_direction_time_format,
                point.directionLabel,
                timeFormat.format(Date(point.timestamp))
            )
            binding.itemUserLine.text = context.getString(R.string.history_user_format, point.userId)
        }

        private fun threatColorRes(level: ThreatLevel) = when (level) {
            ThreatLevel.THREAT -> R.color.status_offline
            ThreatLevel.ATTENTION -> R.color.status_queued
            ThreatLevel.OBSERVATION -> R.color.status_online
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PendingPointEntity>() {
            override fun areItemsTheSame(oldItem: PendingPointEntity, newItem: PendingPointEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PendingPointEntity, newItem: PendingPointEntity) =
                oldItem == newItem
        }
    }
}
