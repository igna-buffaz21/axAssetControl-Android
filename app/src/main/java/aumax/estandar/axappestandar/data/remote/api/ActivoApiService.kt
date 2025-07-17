package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.Activos.Activo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ActivoApiService {
    @GET("api/SubSector/ObtenerPorTag")
    suspend fun obtenerActivos(@Query("tagRfid") tagRfid: String) : Response<List<Activo>>
}