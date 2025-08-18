package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.SubSector.ResponseAsignarTagSS
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SubSectorApiService {
    @GET("api/SubSector/Sector/{id}")
    suspend fun obtenerSubsectores(@Path("id") id: Int,
                                   @Query("idEmpresa") idEmpresa: Int,
                                   @Query("status") status : Boolean ) : Response<List<SubSector>>

    @PUT("api/SubSector/AsignarTagRfid")
    suspend fun asignarTagRfid(@Query("Rfid") rfid: String,
                               @Query("idSubSector") idSubSector: Int,
                               @Query("idEmpresa") idEmpresa: Int) : Response<ResponseAsignarTagSS>

    @GET("api/SubSector/ObtenerSubSectorPorRFID")
    suspend fun obtenerSubSectorPorRfid(@Query("Rfid") Rfid: String,
                                        @Query("idEmpresa") idEmpresa: Int ) : Response<SubSector>

}