package com.example.operator.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Тактильная обратная связь оператору по уровню угрозы при отправке точки. */
object VibrationManager {

    fun vibrate(context: Context, threatLevel: String) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        val pattern = when (threatLevel) {
            // УГРОЗА: серия из 4 сильных импульсов
            "THREAT" -> longArrayOf(0, 300, 100, 300, 100, 300, 100, 600)
            // ВНИМАНИЕ: два средних импульса
            "ATTENTION" -> longArrayOf(0, 200, 150, 200)
            // НАБЛЮДЕНИЕ: один короткий импульс
            "OBSERVATION" -> longArrayOf(0, 100)
            else -> longArrayOf(0, 100)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = when (threatLevel) {
                "THREAT" -> intArrayOf(0, 255, 0, 255, 0, 255, 0, 255)
                "ATTENTION" -> intArrayOf(0, 180, 0, 180)
                "OBSERVATION" -> intArrayOf(0, 80)
                else -> intArrayOf(0, 80)
            }
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}
