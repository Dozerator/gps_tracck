package com.example.operator.ui

import android.Manifest
import android.animation.AnimatorInflater
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.operator.OperatorApp
import com.example.operator.R
import com.example.operator.data.repository.SendResult
import com.example.operator.databinding.ActivityMainBinding
import com.example.operator.databinding.DialogCompassBinding
import com.example.operator.databinding.DialogConfirmBinding
import com.example.operator.databinding.DialogObjectTypeBinding
import com.example.operator.databinding.DialogThreatBinding
import com.example.operator.model.ObjectType
import com.example.operator.model.ThreatLevel
import com.example.operator.service.LocationService
import com.example.operator.utils.VibrationManager
import com.example.operator.utils.directionLabel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val markPointViewModel: MarkPointViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModel.Factory((application as OperatorApp).locationRepository)
    }

    private var userLocationMarker: Marker? = null
    private var pendingPointMarker: Marker? = null
    private var lastKnownLocation: Location? = null
    private var pendingSendObjectType: ObjectType? = null

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

        setupMap()
        binding.markPointButton.setOnClickListener { onMarkPointClicked() }
        binding.statusBar.setOnClickListener {
            startActivity(Intent(this, QueueActivity::class.java))
        }

        requestNotificationPermissionIfNeeded()
        requestForegroundPermissionsIfNeeded()
        observeLocationUpdates()
        observeSyncStatus()
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

        markPointViewModel.reset()
        markPointViewModel.location = location
        showPendingMarker(location)
        showObjectTypeDialog()
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

    // Шаг 1: выбор типа объекта.
    private fun showObjectTypeDialog() {
        val dialogBinding = DialogObjectTypeBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.uavButton.setOnClickListener {
            markPointViewModel.objectType = ObjectType.UAV
            dialog.dismiss()
            showCompassDialog()
        }
        dialogBinding.quadButton.setOnClickListener {
            markPointViewModel.objectType = ObjectType.QUAD
            dialog.dismiss()
            showCompassDialog()
        }
        dialogBinding.objectTypeCancelButton.setOnClickListener {
            dialog.dismiss()
            cancelMarking()
        }
        dialog.show()
    }

    // Шаг 2: направление движения — компас с точностью до градуса (вращение пальцем).
    private fun showCompassDialog() {
        val dialogBinding = DialogCompassBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Если возвращаемся назад из последующих шагов — восстанавливаем ранее выбранный угол.
        val initialDegrees = markPointViewModel.directionDegrees ?: 0
        dialogBinding.compassView.angleDegrees = initialDegrees.toFloat()
        dialogBinding.tvAngle.text = getString(R.string.compass_degrees_format, initialDegrees)
        dialogBinding.tvDirectionLabel.text = directionLabel(initialDegrees)

        dialogBinding.compassView.onAngleChanged = { angle ->
            val degrees = angle.toInt()
            dialogBinding.tvAngle.text = getString(R.string.compass_degrees_format, degrees)
            dialogBinding.tvDirectionLabel.text = directionLabel(degrees)
        }

        dialogBinding.btnN.setOnClickListener { dialogBinding.compassView.setAngleAnimated(0f) }
        dialogBinding.btnE.setOnClickListener { dialogBinding.compassView.setAngleAnimated(90f) }
        dialogBinding.btnS.setOnClickListener { dialogBinding.compassView.setAngleAnimated(180f) }
        dialogBinding.btnW.setOnClickListener { dialogBinding.compassView.setAngleAnimated(270f) }

        // Точная настройка: тап ±1°, долгое нажатие ±5°.
        dialogBinding.btnMinus.setOnClickListener { dialogBinding.compassView.angleDegrees -= 1f }
        dialogBinding.btnPlus.setOnClickListener { dialogBinding.compassView.angleDegrees += 1f }
        dialogBinding.btnMinus.setOnLongClickListener { dialogBinding.compassView.angleDegrees -= 5f; true }
        dialogBinding.btnPlus.setOnLongClickListener { dialogBinding.compassView.angleDegrees += 5f; true }

        dialogBinding.compassConfirmButton.setOnClickListener {
            markPointViewModel.directionDegrees = dialogBinding.compassView.angleDegrees.toInt()
            dialog.dismiss()
            showThreatDialog()
        }
        dialogBinding.compassBackButton.setOnClickListener {
            dialog.dismiss()
            showObjectTypeDialog()
        }
        dialogBinding.compassCancelButton.setOnClickListener {
            dialog.dismiss()
            cancelMarking()
        }
        dialog.show()
    }

    // Шаг 3: оценка уровня угрозы.
    private fun showThreatDialog() {
        val dialogBinding = DialogThreatBinding.inflate(LayoutInflater.from(this))

        dialogBinding.threatObservationTitle.text = ThreatLevel.OBSERVATION.label
        dialogBinding.threatObservationSubtitle.text = ThreatLevel.OBSERVATION.subtitle
        dialogBinding.threatAttentionTitle.text = ThreatLevel.ATTENTION.label
        dialogBinding.threatAttentionSubtitle.text = ThreatLevel.ATTENTION.subtitle
        dialogBinding.threatThreatTitle.text = ThreatLevel.THREAT.label
        dialogBinding.threatThreatSubtitle.text = ThreatLevel.THREAT.subtitle

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Пульс запускается сразу при показе диалога и останавливается при его закрытии,
        // чтобы аниматор не продолжал работать на отсоединённой view (утечка).
        val pulseAnimator = AnimatorInflater.loadAnimator(this, R.animator.pulse_animation).apply {
            setTarget(dialogBinding.threatThreatRow)
        }
        dialog.setOnShowListener { pulseAnimator.start() }
        dialog.setOnDismissListener { pulseAnimator.cancel() }

        val chooseThreat: (ThreatLevel) -> Unit = { threatLevel ->
            markPointViewModel.threatLevel = threatLevel
            dialog.dismiss()
            showFinalConfirmDialog()
        }
        dialogBinding.threatObservationRow.setOnClickListener { chooseThreat(ThreatLevel.OBSERVATION) }
        dialogBinding.threatAttentionRow.setOnClickListener { chooseThreat(ThreatLevel.ATTENTION) }
        dialogBinding.threatThreatRow.setOnClickListener { chooseThreat(ThreatLevel.THREAT) }

        dialogBinding.threatBackButton.setOnClickListener {
            dialog.dismiss()
            showCompassDialog()
        }
        dialogBinding.threatCancelButton.setOnClickListener {
            dialog.dismiss()
            cancelMarking()
        }
        dialog.show()
    }

    // Шаг 4: подтверждение и отправка данных оператору.
    private fun showFinalConfirmDialog() {
        val location = markPointViewModel.location ?: return
        val objectType = markPointViewModel.objectType ?: return
        val directionDegrees = markPointViewModel.directionDegrees ?: return
        val threatLevel = markPointViewModel.threatLevel ?: return

        val dialogBinding = DialogConfirmBinding.inflate(LayoutInflater.from(this))
        dialogBinding.dialogTitle.text = if (threatLevel == ThreatLevel.THREAT) {
            getString(R.string.confirm_point_title_threat)
        } else {
            getString(R.string.confirm_point_title)
        }
        dialogBinding.dialogTypeLine.text = getString(R.string.confirm_point_type, objectType.label)
        dialogBinding.dialogDirectionLine.text =
            getString(R.string.confirm_point_direction, directionLabel(directionDegrees))
        dialogBinding.dialogThreatLine.text =
            getString(R.string.confirm_point_threat, threatLevel.icon, threatLevel.label)
        dialogBinding.dialogCoordinates.text =
            getString(R.string.confirm_point_coordinates, location.latitude, location.longitude)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // При УГРОЗЕ кнопка отправки мигает красным, как и предупреждение в заголовке.
        val sendButtonPulse = if (threatLevel == ThreatLevel.THREAT) {
            dialogBinding.confirmSendButton.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(ThreatLevel.THREAT.colorHex))
            AnimatorInflater.loadAnimator(this, R.animator.pulse_animation).apply {
                setTarget(dialogBinding.confirmSendButton)
            }
        } else {
            null
        }
        dialog.setOnShowListener { sendButtonPulse?.start() }
        dialog.setOnDismissListener { sendButtonPulse?.cancel() }

        dialogBinding.confirmSendButton.setOnClickListener {
            dialog.dismiss()
            VibrationManager.vibrate(this, threatLevel.apiValue)
            sendLocationPoint(location, objectType, directionDegrees, directionLabel(directionDegrees), threatLevel)
        }
        dialogBinding.confirmCancelButton.setOnClickListener {
            dialog.dismiss()
            cancelMarking()
        }
        dialog.show()
    }

    private fun cancelMarking() {
        pendingPointMarker?.let { binding.mapView.overlays.remove(it) }
        pendingPointMarker = null
        binding.mapView.invalidate()
        markPointViewModel.reset()
    }

    // Отправка идёт через MainViewModel/LocationRepository: онлайн — сразу на сервер,
    // офлайн (или ошибка сети) — в локальную очередь Room с последующей автосинхронизацией.
    private fun sendLocationPoint(
        location: Location,
        objectType: ObjectType,
        directionDegrees: Int,
        directionLabelText: String,
        threatLevel: ThreatLevel
    ) {
        pendingSendObjectType = objectType
        mainViewModel.sendPoint(
            lat = location.latitude,
            lon = location.longitude,
            accuracy = location.accuracy,
            timestampMillis = location.time,
            objectType = objectType,
            directionDegrees = directionDegrees,
            directionLabel = directionLabelText,
            threatLevel = threatLevel
        )
        markPointViewModel.reset()
    }

    private fun markPendingPointAsSent(objectType: ObjectType) {
        val iconRes = if (objectType == ObjectType.UAV) R.drawable.ic_marker_uav else R.drawable.ic_marker_quad
        pendingPointMarker?.icon = ContextCompat.getDrawable(this, iconRes)
        binding.mapView.invalidate()
    }

    // Статус сети, размер локальной очереди и результат последней отправки.
    private fun observeSyncStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.isOnline.collect { online ->
                        if (online) {
                            binding.statusBar.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.status_online))
                            binding.statusText.text = getString(R.string.status_online)
                            delay(3000)
                            if (mainViewModel.pendingCount.value == 0) {
                                binding.statusBar.visibility = View.GONE
                            }
                        } else {
                            binding.statusBar.visibility = View.VISIBLE
                            binding.statusBar.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.status_offline))
                            binding.statusText.text = getString(R.string.status_offline)
                        }
                    }
                }
                launch {
                    mainViewModel.pendingCount.collect { count ->
                        if (count > 0) {
                            binding.statusBar.visibility = View.VISIBLE
                            binding.queueCount.text = getString(R.string.status_queue_count, count)
                        } else {
                            binding.queueCount.text = ""
                        }
                    }
                }
                launch {
                    mainViewModel.sendResult.collect { result ->
                        when (result) {
                            is SendResult.SentOnline -> {
                                pendingSendObjectType?.let { markPendingPointAsSent(it) }
                                showSnackbar(getString(R.string.send_result_online), R.color.status_online)
                            }
                            is SendResult.SavedOffline -> {
                                pendingSendObjectType?.let { markPendingPointAsSent(it) }
                                showSnackbar(getString(R.string.send_result_offline), R.color.status_queued)
                            }
                            is SendResult.Error -> {
                                showSnackbar(getString(R.string.send_result_error, result.message), R.color.status_offline)
                            }
                        }
                        pendingSendObjectType = null
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String, colorRes: Int) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, colorRes))
        snackbar.show()
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
    }
}
