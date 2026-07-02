package com.example.operator.network

import android.content.Context
import android.util.Log
import com.example.operator.BuildConfig
import com.example.operator.security.TlsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

/**
 * Клиент WSS-канала /ws/operator поверх того же TLS/pinning-стека, что и REST API.
 *
 * В текущем флоу приложения не используется: точки уходят от телефона на сервер
 * по REST (POST /api/location/point), а живой поток new_point слушает веб-панель
 * оператора в браузере (backend/static/index.html), не сам Android-клиент. Класс
 * оставлен готовым к работе на случай, если понадобится показывать оператору
 * входящие точки прямо в приложении (например, экран "живая лента").
 */
class WebSocketManager(context: Context) {

    private val client = TlsManager.buildOkHttpClient(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var webSocket: WebSocket? = null
    private var manuallyClosed = false

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val messages: SharedFlow<String> = _messages

    fun connect() {
        manuallyClosed = false
        val request = Request.Builder()
            .url(BuildConfig.WS_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Защищённое WSS-соединение установлено")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch { _messages.emit(text) }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Ошибка WSS-соединения: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!manuallyClosed) scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (manuallyClosed) return
        scope.launch {
            delay(RECONNECT_DELAY_MS)
            connect()
        }
    }

    fun disconnect() {
        manuallyClosed = true
        webSocket?.close(1000, "Disconnect")
        webSocket = null
    }

    companion object {
        private const val TAG = "WebSocketManager"
        private const val RECONNECT_DELAY_MS = 5000L
    }
}
