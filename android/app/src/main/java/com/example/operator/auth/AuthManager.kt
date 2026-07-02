package com.example.operator.auth

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.Base64

/**
 * Хранит JWT-токен в SharedPreferences и умеет проверять его срок годности
 * по полю "exp" из payload токена — без обращения к серверу.
 */
class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean {
        val token = getToken() ?: return false
        return !isTokenExpired(token)
    }

    /** Логин текущего пользователя из поля "sub" JWT-токена, для локального отображения. */
    fun getUserLogin(): String? {
        val token = getToken() ?: return null
        return decodePayload(token)?.optString("sub")?.ifEmpty { null }
    }

    private fun isTokenExpired(token: String): Boolean {
        val payload = decodePayload(token) ?: return true
        val exp = payload.optLong("exp", 0L)
        val nowSeconds = System.currentTimeMillis() / 1000
        return exp <= nowSeconds
    }

    private fun decodePayload(token: String): JSONObject? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            JSONObject(payloadJson)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "operator_auth_prefs"
        private const val KEY_TOKEN = "jwt_token"
    }
}
