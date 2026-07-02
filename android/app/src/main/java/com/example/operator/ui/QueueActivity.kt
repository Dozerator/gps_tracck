package com.example.operator.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.operator.OperatorApp
import com.example.operator.R
import com.example.operator.databinding.ActivityQueueBinding
import kotlinx.coroutines.launch

/** Экран локальной очереди отправки: список точек, ожидающих синхронизации, и ручной запуск синка. */
class QueueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQueueBinding
    private val adapter = QueuePointAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = (application as OperatorApp).locationRepository

        binding.queueToolbar.setNavigationOnClickListener { finish() }
        binding.queueRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.queueRecyclerView.adapter = adapter

        binding.syncNowButton.setOnClickListener {
            lifecycleScope.launch { repository.retryFailed() }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.allPoints.collect { points ->
                    adapter.submitList(points)
                    binding.queueToolbar.title = getString(R.string.queue_screen_title, points.size)
                    val isEmpty = points.isEmpty()
                    binding.queueEmptyText.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    binding.queueRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                }
            }
        }
    }
}
