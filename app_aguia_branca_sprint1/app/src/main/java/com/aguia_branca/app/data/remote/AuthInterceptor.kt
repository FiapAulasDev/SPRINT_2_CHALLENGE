package com.aguia_branca.app.data.remote

import com.aguia_branca.app.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val isLoginRequest = original.url.encodedPath.endsWith("/api/auth/login")
        val token = TokenStore.getToken()

        val request = if (!isLoginRequest && token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        if (response.code == 401 && !isLoginRequest && token != null) {
            TokenStore.clearToken()
            SessionEvents.notifySessionExpired()
        }

        return response
    }
}
