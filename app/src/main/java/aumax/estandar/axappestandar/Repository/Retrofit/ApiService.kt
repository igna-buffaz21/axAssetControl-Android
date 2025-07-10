package aumax.estandar.axappestandar.Repository.Retrofit

//import androidx.room.Query
import aumax.estandar.axappestandar.Models.LoginRequest
import aumax.estandar.axappestandar.Models.LoginResponse
import aumax.estandar.axappestandar.Models.User
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/Auth/login")
    suspend fun login (@Body loginRequest: LoginRequest): retrofit2.Response<LoginResponse>

    @GET("api/Usuario/ObtenerUsuariosPorCompania/{idCompany}")
    suspend fun getUsers(
        @Path("idCompany") idCompany: Int,
        @retrofit2.http.Query("status") status: String
    ) : retrofit2.Response<List<User>>

    /*@POST("api/Usuario/CrearUsuarioEnCantidad")
    suspend fun syncUserToApi(@Body usuarios: List<SyncUserRequest>) : retrofit2.Response<Unit>*/
}