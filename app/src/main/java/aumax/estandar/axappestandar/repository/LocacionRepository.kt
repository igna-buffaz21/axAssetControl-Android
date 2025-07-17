package aumax.estandar.axappestandar.repository

import android.util.Log
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.models.Locacion.Locacion
import aumax.estandar.axappestandar.data.remote.api.LocacionApiService

class LocacionRepository(
    private val tokenManager: TokenManager,
    private val locacionApiService: LocacionApiService
) {
    suspend fun obtenerLocaciones(id: Int, status: Boolean) : Result<List<Locacion>?> {
        return try {
            val response = locacionApiService.obtenerLocaciones(id, status)

            if (response.isSuccessful) {
                val subSectores = response.body()
                Result.success(subSectores)
            }
            else {
                Result.failure(Exception("Error"))
            }
        }
        catch (e: Exception) {
            Log.e("Token Guardado", "Excepción en login: ${e.message}")
            Result.failure(e)
        }

    }
}