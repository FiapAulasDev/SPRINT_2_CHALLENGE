package com.aguia_branca.app.data.remote

import com.aguia_branca.app.data.remote.dto.ErroResponse
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: HttpException) {
        val mensagem = e.response()?.errorBody()?.string()?.let { body ->
            runCatching { RetrofitClient.gson.fromJson(body, ErroResponse::class.java) }
                .getOrNull()
                ?.mensagem
        }
        Result.failure(ApiException(e.code(), mensagem ?: "Erro inesperado (${e.code()})"))
    } catch (e: IOException) {
        Result.failure(ApiException(-1, "Falha de conexão. Verifique sua internet."))
    }
}
