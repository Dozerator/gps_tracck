package com.example.operator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.operator.R
import com.example.operator.auth.AuthManager
import com.example.operator.databinding.ActivityMainBinding
import com.example.operator.model.LocationPointRequest
import com.example.operator.network.RetrofitClient
import com.example.operator.service.LocationService
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var authManager: AuthManager

    private var userLocationMarker: Marker? = null
    private var pendingPointMarker: Marker? = null
    private var lastKnownLocation: Location? = null

    private val foregroundLocationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestForegroundPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                onForegroundLocationGranted()
            } else {
                Toast.makeText(this, R.string.location_permission_rationale, Toast.LENGTH_LONG).show()
            }
        }

    private val requestBackgroundPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Фоновое отслеживание — опционально; сервис уже запущен в foreground-режиме.
        }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        Configuration.getInstance().load(
            this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE)
        )
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authManager = AuthManager(this)

        setupMap()
        binding.markPointButton.setOnClickListener { onMarkPointClicked() }

        requestNotificationPermissionIfNeeded()
        requestForegroundPermissionsIfNeeded()
        observeLocationUpdates()
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.controller.setZoom(16.0)
        binding.mapView.controller.setCenter(GeoPoint(DEFAULT_LAT, DEFAULT_LON))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestForegroundPermissionsIfNeeded() {
        val hasFine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            onForegroundLocationGranted()
        } else {
            requestForegroundPermissions.launch(foregroundLocationPermissions)
        }
    }

    private fun onForegroundLocationGranted() {
        LocationService.start(this)
        requestBackgroundPermissionIfNeeded()
    }

    private fun requestBackgroundPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val hasBackground = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasBackground) {
                Toast.makeText(this, R.string.background_location_rationale, Toast.LENGTH_LONG).show()
                requestBackgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun observeLocationUpdates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                LocationService.currentLocation.collect { location ->
                    if (location != null) {
                        lastKnownLocation = location
                        updateUserLocationMarker(location)
                    }
                    binding.locationProgress.visibility = if (location == null) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun updateUserLocationMarker(location: Location) {
        val point = GeoPoint(location.latitude, location.longitude)

        var marker = userLocationMarker
        if (marker == null) {
            marker = Marker(binding.mapView).apply {
                icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_dot_blue)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Вы здесь"
            }
            binding.mapView.overlays.add(marker)
            userLocationMarker = marker
            binding.mapView.controller.setCenter(point)
        }
        marker.position = point
        binding.mapView.invalidate()
    }

    private fun onMarkPointClicked() {
        val location = lastKnownLocation
        if (location == null) {
            Toast.makeText(this, R.string.location_permission_rationale, Toast.LENGTH_SHORT).show()
            return
        }

        showPendingMarker(location)
        showConfirmDialog(location)
    }

    private fun showPendingMarker(location: Location) {
        val point = GeoPoint(location.latitude, location.longitude)
        pendingPointMarker?.let { binding.mapView.overlays.remove(it) }

        val marker = Marker(binding.mapView).apply {
            position = point
            icon = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_marker_blue)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Отмеченная точка"
        }
        binding.mapView.overlays.add(marker)
        pendingPointMarker = marker
        binding.mapView.invalidate()
    }

    private fun showConfirmDialog(location: Location) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null)
        dialogView.findViewById<TextView>(R.id.dialogCoordinates).text =
            getString(R.string.confirm_point_message, location.latitude, location.longitude)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.confirm_button) { dialog, _ ->
                sendLocationPoint(location)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel_button) { dialog, _ ->
                pendingPointMarker?.let { binding.mapView.overlays.remove(it) }
                pendingPointMarker = null
                binding.mapView.invalidate()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun sendLocationPoint(location: Location) {
        val token = authManager.getToken() ?: return
        val timestamp = ISO_FORMAT.format(java.util.Date(location.time))

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.sendLocationPoint(
                    "Bearer $token",
                    LocationPointRequest(
                        lat = location.latitude,
                        lon = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = timestamp
                    )
                )
                if (response.isSuccessful) {
                    markPendingPointAsSent()
                    Toast.makeText(this@MainActivity, R.string.point_sent_toast, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, R.string.point_send_error, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, R.string.point_send_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun markPendingPointAsSent() {
        pendingPointMarker?.icon = ContextCompat.getDrawable(this, R.drawable.ic_marker_green)
        binding.mapView.invalidate()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    companion object {
        private const val DEFAULT_LAT = 55.7558
        private const val DEFAULT_LON = 37.6173

        private val ISO_FORMAT = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
