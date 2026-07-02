package com.example.operator

import android.app.Application
import org.osmdroid.config.Configuration

class OperatorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().apply {
            load(this@OperatorApp, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))
            userAgentValue = packageName
        }
    }
}
