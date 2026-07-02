package com.example.operator.network

import android.content.Context
import com.example.operator.BuildConfig
import com.example.operator.security.TlsManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Не object-синглтон, а обычный класс: OkHttpClient должен быть собран с Context
 * (нужен для чтения самоподписанного сертификата из res/raw при HTTPS). Держится
 * как единственный экземпляр в [com.example.operator.OperatorApp].
 */
class RetrofitClient(context: Context) {

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL.trimEnd('/') + "/")
            .client(TlsManager.buildOkHttpClient(context.applicationContext))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
