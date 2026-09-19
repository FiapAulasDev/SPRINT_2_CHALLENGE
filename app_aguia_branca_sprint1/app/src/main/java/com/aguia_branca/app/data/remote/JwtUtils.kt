package com.aguia_branca.app.data.remote

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class DecodedJwtPayload(
    @SerializedName("sub") val subject: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("exp") val expiresAt: Long?
)

object JwtUtils {

    private val gson = Gson()

    fun decode(token: String): DecodedJwtPayload? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payloadSegment = parts[1]
            val padded = payloadSegment.padEnd(
                payloadSegment.length + (4 - payloadSegment.length % 4) % 4,
                '='
            )
            val decodedBytes = Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP)
            val json = String(decodedBytes, Charsets.UTF_8)
            gson.fromJson(json, DecodedJwtPayload::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isExpired(payload: DecodedJwtPayload): Boolean {
        val exp = payload.expiresAt ?: return true
        val nowInSeconds = System.currentTimeMillis() / 1000
        return exp <= nowInSeconds
    }
}
