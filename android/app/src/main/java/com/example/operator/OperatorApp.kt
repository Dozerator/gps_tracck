package com.example.operator

import android.app.Application
import com.example.operator.auth.AuthManager
import com.example.operator.data.local.AppDatabase
import com.example.operator.data.repository.LocationRepository
import com.example.operator.network.RetrofitClient
import com.example.operator.utils.NetworkMonitor
import org.osmdroid.config.Configuration

class OperatorApp : Application() {

    val authManager: AuthManager by lazy { AuthManager(this) }
    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor(this) }
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val locationRepository: LocationRepository by lazy {
        LocationRepository(
            dao = database.pendingPointDao(),
            apiService = RetrofitClient.apiService,
            authManager = authManager,
            networkMonitor = networkMonitor,
            context = this
        )
    }

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            load(this@OperatorApp, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}
