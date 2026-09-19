package com.aguia_branca.app.data.remote

import com.aguia_branca.app.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val isPublicAuthRequest = original.url.encodedPath.endsWith("/api/auth/login") ||
            original.url.encodedPath.endsWith("/api/auth/register")
        val token = TokenStore.getToken()

        val request = if (!isPublicAuthRequest && token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        if (response.code == 401 && !isPublicAuthRequest && token != null) {
            TokenStore.clearToken()
            SessionEvents.notifySessionExpired()
        }

        return response
    }
}
