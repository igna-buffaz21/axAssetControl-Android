package aumax.estandar.axappestandar.repository

import android.util.Log
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.data.remote.api.SubSectorApiService

class SubSectorRepository(
    private val tokenManager: TokenManager,
    private val subSectorApiService: SubSectorApiService
) {

    suspend fun obtenerSectores(idSector: Int, idEmpresa: Int) : Result<List<SubSector>?> {
        return try {
            val response = subSectorApiService.obtenerSubsectores(idSector, idEmpresa, true)

            if (response.isSuccessful) {
                val subsectores = response.body()
                Log.d("SUBSECTORES", "okey: ${subsectores}")
                Result.success(subsectores)
            }
            else {
                Log.d("SUBSECTORES", "algo salio mal ${response}")
                Result.failure(Exception("Error"))
            }
        }
        catch (e: Exception) {
            Log.d("SUBSECTORES", "algo salio mal catch ${e}")
            Result.failure(e)
        }
    }

}