package com.example.operator.export

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.operator.BuildConfig
import com.example.operator.data.local.entity.PendingPointEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экспорт локальной истории точек и передача файла во внешние приложения.
 * Приоритет получателя: TrueConf → Telegram → системное меню "Поделиться"
 * (проверка через PackageManager + откат по ActivityNotFoundException).
 */
object ExportManager {
    private const val TRUECONF_PACKAGE = "ru.trueconf.client"
    private const val TELEGRAM_PACKAGE = "org.telegram.messenger"

    /** Строит GeoJSON (RFC 7946) через JSONObject/JSONArray — без ручной сборки строк,
     * которая могла бы сломаться на кавычках/спецсимволах в текстовых полях. */
    fun buildGeoJson(points: List<PendingPointEntity>): String {
        val features = JSONArray()
        points.forEach { p ->
            val geometry = JSONObject().apply {
                put("type", "Point")
                put("coordinates", JSONArray().put(p.lon).put(p.lat))
            }
            val properties = JSONObject().apply {
                put("id", p.id)
                put("user_id", p.userId)
                put("object_type", p.objectType)
                put("direction_degrees", p.directionDegrees)
                put("direction_label", p.directionLabel)
                put("threat_level", p.threatLevel)
                put("track_id", p.trackId)
                put("status", p.status)
                put("timestamp", isoFormat(p.timestamp))
            }
            features.put(
                JSONObject().apply {
                    put("type", "Feature")
                    put("geometry", geometry)
                    put("properties", properties)
                }
            )
        }
        return JSONObject().apply {
            put("type", "FeatureCollection")
            put("features", features)
        }.toString(2)
    }

    fun exportToGeoJson(context: Context, points: List<PendingPointEntity>): File {
        val json = buildGeoJson(points)
        val file = File(exportDir(context), "operator_export_${fileTimestamp()}.geojson")
        file.writeText(json)
        return file
    }

    fun exportDir(context: Context): File = File(context.cacheDir, "exports").apply { mkdirs() }

    fun fileTimestamp(): String = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())

    /** Делится файлом, отдавая приоритет TrueConf, затем Telegram, затем системному чузеру. */
    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        val intent = buildShareIntent(uri, mimeType)

        if (isAppInstalled(context, TRUECONF_PACKAGE)) {
            intent.setPackage(TRUECONF_PACKAGE)
            try {
                context.startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                intent.setPackage(null)
            }
        }

        if (isAppInstalled(context, TELEGRAM_PACKAGE)) {
            intent.setPackage(TELEGRAM_PACKAGE)
            try {
                context.startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                intent.setPackage(null)
            }
        }

        context.startActivity(Intent.createChooser(intent, null))
    }

    private fun buildShareIntent(uri: Uri, mimeType: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    private fun isAppInstalled(context: Context, packageName: String): Boolean =
        try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

    private fun isoFormat(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date(millis))
}
