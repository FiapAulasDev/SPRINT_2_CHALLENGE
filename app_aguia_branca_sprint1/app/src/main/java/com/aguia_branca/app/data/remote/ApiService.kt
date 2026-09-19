package com.aguia_branca.app.data.remote

import com.aguia_branca.app.data.remote.dto.AndamentoProjetoRequest
import com.aguia_branca.app.data.remote.dto.AuthenticationRequest
import com.aguia_branca.app.data.remote.dto.AvaliacaoIdeiaRequest
import com.aguia_branca.app.data.remote.dto.DashboardResumoResponse
import com.aguia_branca.app.data.remote.dto.EstrategiaRequest
import com.aguia_branca.app.data.remote.dto.EstrategiaResponse
import com.aguia_branca.app.data.remote.dto.EtapaProjeto
import com.aguia_branca.app.data.remote.dto.IdeiaRequest
import com.aguia_branca.app.data.remote.dto.IdeiaResponse
import com.aguia_branca.app.data.remote.dto.LoginResponse
import com.aguia_branca.app.data.remote.dto.ProjetoRequest
import com.aguia_branca.app.data.remote.dto.ProjetoResponse
import com.aguia_branca.app.data.remote.dto.ResumoIdeiasResponse
import com.aguia_branca.app.data.remote.dto.ResumoProjetosResponse
import com.aguia_branca.app.data.remote.dto.StatusIdeia
import com.aguia_branca.app.data.remote.dto.StatusProjeto
import com.aguia_branca.app.data.remote.dto.UsuarioResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: AuthenticationRequest): LoginResponse

    @GET("api/usuarios/me")
    suspend fun meuUsuario(): UsuarioResponse

    @GET("api/usuarios/ranking")
    suspend fun rankingUsuarios(): List<UsuarioResponse>

    @GET("api/estrategias")
    suspend fun listarEstrategias(): List<EstrategiaResponse>

    @GET("api/estrategias/vigente")
    suspend fun buscarEstrategiaVigente(): EstrategiaResponse

    @GET("api/estrategias/{id}")
    suspend fun buscarEstrategiaPorId(@Path("id") id: String): EstrategiaResponse

    @POST("api/estrategias")
    suspend fun criarEstrategia(@Body body: EstrategiaRequest): EstrategiaResponse

    @PUT("api/estrategias/{id}")
    suspend fun atualizarEstrategia(@Path("id") id: String, @Body body: EstrategiaRequest): EstrategiaResponse

    @DELETE("api/estrategias/{id}")
    suspend fun excluirEstrategia(@Path("id") id: String)

    @GET("api/ideias")
    suspend fun listarIdeias(@Query("status") status: StatusIdeia? = null): List<IdeiaResponse>

    @GET("api/ideias/minhas")
    suspend fun listarMinhasIdeias(): List<IdeiaResponse>

    @GET("api/ideias/ranking")
    suspend fun rankingIdeias(@Query("status") status: StatusIdeia? = null): List<IdeiaResponse>

    @GET("api/ideias/{id}")
    suspend fun buscarIdeiaPorId(@Path("id") id: String): IdeiaResponse

    @POST("api/ideias")
    suspend fun criarIdeia(@Body body: IdeiaRequest): IdeiaResponse

    @PUT("api/ideias/{id}")
    suspend fun atualizarIdeia(@Path("id") id: String, @Body body: IdeiaRequest): IdeiaResponse

    @DELETE("api/ideias/{id}")
    suspend fun excluirIdeia(@Path("id") id: String)

    @POST("api/ideias/{id}/analise-ia")
    suspend fun analisarIdeiaComIa(@Path("id") id: String): IdeiaResponse

    @PATCH("api/ideias/{id}/avaliacao")
    suspend fun avaliarIdeia(@Path("id") id: String, @Body body: AvaliacaoIdeiaRequest): IdeiaResponse

    @GET("api/projetos")
    suspend fun listarProjetos(
        @Query("status") status: StatusProjeto? = null,
        @Query("etapa") etapa: EtapaProjeto? = null
    ): List<ProjetoResponse>

    @GET("api/projetos/{id}")
    suspend fun buscarProjetoPorId(@Path("id") id: String): ProjetoResponse

    @POST("api/projetos")
    suspend fun criarProjeto(@Body body: ProjetoRequest): ProjetoResponse

    @PUT("api/projetos/{id}")
    suspend fun atualizarProjeto(@Path("id") id: String, @Body body: ProjetoRequest): ProjetoResponse

    @PATCH("api/projetos/{id}/andamento")
    suspend fun atualizarAndamentoProjeto(@Path("id") id: String, @Body body: AndamentoProjetoRequest): ProjetoResponse

    @DELETE("api/projetos/{id}")
    suspend fun excluirProjeto(@Path("id") id: String)

    @GET("api/dashboard/resumo")
    suspend fun dashboardResumo(): DashboardResumoResponse

    @GET("api/dashboard/ideias")
    suspend fun dashboardIdeias(): ResumoIdeiasResponse

    @GET("api/dashboard/projetos")
    suspend fun dashboardProjetos(): ResumoProjetosResponse
}
