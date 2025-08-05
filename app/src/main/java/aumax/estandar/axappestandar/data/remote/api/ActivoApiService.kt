package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.Activos.ResponseAsignarTagActivo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ActivoApiService {
    @GET("api/SubSector/ObtenerPorTag")
    suspend fun obtenerActivos(@Query("tagRfid") tagRfid: String) : Response<List<Activo>>

    @GET("api/Activo/SubSector/{idsubsector}")
    suspend fun obtenerActivoConIdSubSector(@Path("idsubsector") idSubSector: Int,
                                            @Query("idEmpresa") idEmpresa: Int,
                                            @Query("status") status : Boolean
                                            ) : Response<List<Activo>>
    @PUT("api/Activo/AsignarTagRfid")
    suspend fun asigarTagActivo(@Query("Rfid") Rfid: String,
                                @Query("idActivo") idActivo: Int,
                                @Query("idEmpresa") idEmpresa: Int
    ) : Response<ResponseAsignarTagActivo>

}