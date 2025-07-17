package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.Locacion.Locacion
import jxl.write.Boolean
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocacionApiService {
    @GET("api/Locacion/Empresa/{id}")
    suspend fun obtenerLocaciones(@Path("id") id: Int,
                                  @Query("status") status: kotlin.Boolean) : Response<List<Locacion>>
}