package aumax.estandar.axappestandar.repository

import android.util.Log
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.models.Sector.Sector
import aumax.estandar.axappestandar.data.remote.api.SectorApiService

class SectorRepository(
    private val tokenManager: TokenManager,
    private val sectorApiService: SectorApiService
) {
    suspend fun obtenerSectores(idLocacion : Int, idEmpresa : Int, status : Boolean) : Result<List<Sector>?> {
        return try {
            val response = sectorApiService.obtenerSectores(idLocacion, idEmpresa, status)

            if (response.isSuccessful) {
                val sectores = response.body()
                Log.d("SECTORES", "okey: ${sectores}")
                Result.success(sectores)
            }
            else {
                Log.d("SECTORES", "algo salio mal ${response}")
                Result.failure(Exception("Error"))
            }
        }
        catch (e: Exception) {
            Log.d("SECTORES", "algo salio mal catch ${e}")
            Result.failure(e)
        }
    }
}