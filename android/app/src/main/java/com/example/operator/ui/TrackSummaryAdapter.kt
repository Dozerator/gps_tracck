package com.example.operator.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.operator.R
import com.example.operator.data.local.entity.TrackSummary
import com.example.operator.databinding.ItemTrackSummaryBinding
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackSummaryAdapter : ListAdapter<TrackSummary, TrackSummaryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrackSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), timeFormat)
    }

    class ViewHolder(private val binding: ItemTrackSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: TrackSummary, timeFormat: SimpleDateFormat) {
            val context = binding.root.context
            val objectType = ObjectType.entries.find { it.apiValue == track.objectType }
            val threatLevel = ThreatLevel.entries.find { it.apiValue == track.threatLevel } ?: ThreatLevel.OBSERVATION
            val icon = if (objectType == ObjectType.QUAD) "⬡" else "✈"

            binding.trackObjectLine.text = context.getString(
                R.string.track_object_format,
                icon,
                track.objectType,
                track.pointCount
            )
            binding.trackThreatBadge.text = "● ${threatLevel.label}"
            binding.trackThreatBadge.setTextColor(ContextCompat.getColor(context, threatColorRes(threatLevel)))
            binding.trackTimeRangeLine.text = context.getString(
                R.string.track_time_range_format,
                timeFormat.format(Date(track.startTime)),
                timeFormat.format(Date(track.endTime))
            )
        }

        private fun threatColorRes(level: ThreatLevel) = when (level) {
            ThreatLevel.THREAT -> R.color.status_offline
            ThreatLevel.ATTENTION -> R.color.status_queued
            ThreatLevel.OBSERVATION -> R.color.status_online
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TrackSummary>() {
            override fun areItemsTheSame(oldItem: TrackSummary, newItem: TrackSummary) =
                oldItem.trackId == newItem.trackId

            override fun areContentsTheSame(oldItem: TrackSummary, newItem: TrackSummary) =
                oldItem == newItem
        }
    }
}
