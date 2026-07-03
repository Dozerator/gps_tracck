package com.example.operator.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.operator.BuildConfig
import com.example.operator.OperatorApp
import com.example.operator.R
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.databinding.ActivityHistoryBinding
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/** Экран "История смены": все локально известные точки за последние 12 часов + сводка по трекам. */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val pointAdapter = HistoryPointAdapter()
    private val trackAdapter = TrackSummaryAdapter()

    private var currentTab = 0 // 0 = точки, 1 = треки
    private var lastPoints: List<PendingPointEntity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = pointAdapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnExport.setOnClickListener { exportHistory() }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                binding.rvHistory.adapter = if (currentTab == 0) pointAdapter else trackAdapter
                updateEmptyState()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        val repository = (application as OperatorApp).locationRepository
        val since = System.currentTimeMillis() - SHIFT_WINDOW_MS

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    repository.getShiftHistory(since).collect { points ->
                        lastPoints = points
                        pointAdapter.submitList(points)
                        updateStats(points)
                        if (currentTab == 0) updateEmptyState()
                    }
                }
                launch {
                    repository.getAllTracks().collect { tracks ->
                        trackAdapter.submitList(tracks)
                        if (currentTab == 1) updateEmptyState()
                    }
                }
            }
        }
    }

    private fun updateEmptyState() {
        val isEmpty = if (currentTab == 0) pointAdapter.itemCount == 0 else trackAdapter.itemCount == 0
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun updateStats(points: List<PendingPointEntity>) {
        binding.tvStatTotal.text = getString(R.string.history_stat_total, points.size)
        binding.tvStatThreat.text = getString(
            R.string.history_stat_threat,
            points.count { it.threatLevel == "THREAT" }
        )
        binding.tvStatAttention.text = getString(
            R.string.history_stat_attention,
            points.count { it.threatLevel == "ATTENTION" }
        )
    }

    private fun exportHistory() {
        if (lastPoints.isEmpty()) return

        val csv = buildString {
            appendLine("timestamp,object_type,direction_degrees,direction_label,threat_level,lat,lon,status,track_id")
            val rowFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            lastPoints.forEach { p ->
                appendLine(
                    listOf(
                        rowFormat.format(java.util.Date(p.timestamp)),
                        p.objectType,
                        p.directionDegrees.toString(),
                        p.directionLabel,
                        p.threatLevel,
                        p.lat.toString(),
                        p.lon.toString(),
                        p.status,
                        p.trackId.orEmpty()
                    ).joinToString(",") { field -> "\"${field.replace("\"", "'")}\"" }
                )
            }
        }

        val exportDir = File(cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "operator_history_${System.currentTimeMillis()}.csv")
        file.writeText(csv)

        val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.history_export_button)))
    }

    companion object {
        private const val SHIFT_WINDOW_MS = 12 * 60 * 60 * 1000L
    }
}
