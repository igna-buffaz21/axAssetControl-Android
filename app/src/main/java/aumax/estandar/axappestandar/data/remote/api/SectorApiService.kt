package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.Sector.Sector
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SectorApiService {
    @GET("api/Sector/Locacion/{idcompany}")
    suspend fun obtenerSectores(@Path("idcompany") idLocacion: Int,
                                @Query("idEmpresa") idEmpresa: Int,
                                @Query("status") status: Boolean) : Response<List<Sector>>
}