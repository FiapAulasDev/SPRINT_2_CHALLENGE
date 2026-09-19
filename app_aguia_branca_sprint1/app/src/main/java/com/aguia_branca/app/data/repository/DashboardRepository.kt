package com.aguia_branca.app.data.repository

import com.aguia_branca.app.data.remote.RetrofitClient
import com.aguia_branca.app.data.remote.dto.DashboardResumoResponse
import com.aguia_branca.app.data.remote.safeApiCall

class DashboardRepository {

    suspend fun resumo(): Result<DashboardResumoResponse> =
        safeApiCall { RetrofitClient.api.dashboardResumo() }
}
