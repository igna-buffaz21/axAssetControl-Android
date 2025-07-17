package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.User.LoginRequest
import aumax.estandar.axappestandar.data.models.User.LoginResponse
import aumax.estandar.axappestandar.data.models.User.UsuarioDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApiService {
    @POST("api/Auth/login")
    suspend fun login (@Body loginRequest: LoginRequest): retrofit2.Response<LoginResponse>

    @GET("api/Usuario/obtenerDatosUsuario/{id}")
    suspend fun obtenerDatosUsuario(@Path("id") id: Int) : Response<UsuarioDTO>
}