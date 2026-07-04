package com.example.operator.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.operator.OperatorApp
import com.example.operator.R
import com.example.operator.data.local.entity.PendingPointEntity
import com.example.operator.databinding.ActivityHistoryBinding
import com.example.operator.export.ExportManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val repository by lazy { (application as OperatorApp).locationRepository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = pointAdapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnExport.setOnClickListener { showExportDialog() }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                binding.rvHistory.adapter = if (currentTab == 0) pointAdapter else trackAdapter
                updateEmptyState()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

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

    private fun showExportDialog() {
        val options = arrayOf(
            getString(R.string.history_export_option_geojson),
            getString(R.string.history_export_option_csv),
            getString(R.string.history_export_option_sync),
            getString(R.string.history_export_option_clear)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.history_export_dialog_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportGeoJsonAndShare()
                    1 -> exportCsvAndShare()
                    2 -> triggerServerSync()
                    3 -> confirmClearSyncedHistory()
                }
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    private fun exportGeoJsonAndShare() {
        if (lastPoints.isEmpty()) {
            Toast.makeText(this, R.string.history_export_empty, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) { ExportManager.exportToGeoJson(this@HistoryActivity, lastPoints) }
            ExportManager.shareFile(this@HistoryActivity, file, "application/geo+json")
        }
    }

    private fun exportCsvAndShare() {
        if (lastPoints.isEmpty()) {
            Toast.makeText(this, R.string.history_export_empty, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) { buildCsvFile(lastPoints) }
            ExportManager.shareFile(this@HistoryActivity, file, "text/csv")
        }
    }

    private fun buildCsvFile(points: List<PendingPointEntity>): File {
        val csv = buildString {
            appendLine("timestamp,object_type,direction_degrees,direction_label,threat_level,lat,lon,status,track_id")
            val rowFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            points.forEach { p ->
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

        val file = File(ExportManager.exportDir(this), "operator_history_${ExportManager.fileTimestamp()}.csv")
        file.writeText("﻿" + csv) // UTF-8 BOM — чтобы Excel не путал кодировку
        return file
    }

    private fun triggerServerSync() {
        repository.triggerSync()
        Toast.makeText(this, R.string.history_sync_started, Toast.LENGTH_SHORT).show()
    }

    private fun confirmClearSyncedHistory() {
        AlertDialog.Builder(this)
            .setTitle(R.string.history_clear_confirm_title)
            .setMessage(R.string.history_clear_confirm_message)
            .setPositiveButton(R.string.history_clear_confirm_button) { _, _ ->
                lifecycleScope.launch {
                    repository.clearSyncedHistory()
                    Toast.makeText(this@HistoryActivity, R.string.history_cleared, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel_button, null)
            .show()
    }

    companion object {
        private const val SHIFT_WINDOW_MS = 12 * 60 * 60 * 1000L
    }
}
