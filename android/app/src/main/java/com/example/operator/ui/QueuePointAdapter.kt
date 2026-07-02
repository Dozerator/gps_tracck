package com.example.operator.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.operator.R
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.databinding.ItemQueuePointBinding
import com.example.operator.model.Direction
import com.example.operator.model.ObjectType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QueuePointAdapter : ListAdapter<PendingPointEntity, QueuePointAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQueuePointBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), timeFormat)
    }

    class ViewHolder(private val binding: ItemQueuePointBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(point: PendingPointEntity, timeFormat: SimpleDateFormat) {
            val objectType = ObjectType.entries.find { it.apiValue == point.objectType }
            val direction = Direction.entries.find { it.apiValue == point.direction }

            binding.itemIcon.text = if (objectType == ObjectType.QUAD) "⬡" else "✈"
            binding.itemText.text = binding.root.context.getString(
                R.string.queue_item_format,
                direction?.label.orEmpty(),
                direction?.arrow.orEmpty(),
                timeFormat.format(Date(point.createdAt)),
                point.status
            )
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
