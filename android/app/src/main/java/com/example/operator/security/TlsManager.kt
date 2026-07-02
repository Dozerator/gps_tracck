package com.example.operator.security

import android.content.Context
import android.util.Log
import com.example.operator.BuildConfig
import com.example.operator.R
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Собирает OkHttpClient для общения с backend.
 *
 * Если BuildConfig.SERVER_URL начинается с "https" — клиент доверяет ИСКЛЮЧИТЕЛЬНО
 * нашему самоподписанному сертификату (см. backend/certs/generate_cert.sh) через
 * кастомный TrustManager, плюс Certificate Pinning поверх него как защита от MITM
 * даже в случае компрометации системного хранилища доверенных сертификатов.
 *
 * Если SERVER_URL — обычный http (локальная разработка против эмулятора на
 * 10.0.2.2), TLS-настройка не применяется — иначе соединение с обычным
 * uvicorn-сервером без TLS было бы просто невозможно.
 */
object TlsManager {
    private const val TAG = "TlsManager"

    fun buildOkHttpClient(context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor())

        if (BuildConfig.SERVER_URL.startsWith("https", ignoreCase = true)) {
            applyTls(builder, context)
        }

        return builder.build()
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private fun applyTls(builder: OkHttpClient.Builder, context: Context) {
        val certificate = loadCertificate(context)
        val trustManager = buildTrustManager(certificate)
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), null)
        }

        builder
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { hostname, _ -> hostname.equals(BuildConfig.SERVER_IP, ignoreCase = true) }

        if (BuildConfig.CERT_PIN.isNotBlank()) {
            builder.certificatePinner(
                CertificatePinner.Builder()
                    .add(BuildConfig.SERVER_IP, "sha256/${BuildConfig.CERT_PIN}")
                    .build()
            )
        } else {
            Log.w(TAG, "CERT_PIN не задан в gradle.properties — Certificate Pinning отключён, " +
                "доверие обеспечивается только кастомным trust anchor")
        }
    }

    private fun loadCertificate(context: Context): X509Certificate {
        context.resources.openRawResource(R.raw.server_cert).use { certStream ->
            return CertificateFactory.getInstance("X.509")
                .generateCertificate(certStream) as X509Certificate
        }
    }

    private fun buildTrustManager(certificate: X509Certificate): X509TrustManager {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("server", certificate)
        }
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        ).apply { init(keyStore) }

        return trustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager
    }
}
