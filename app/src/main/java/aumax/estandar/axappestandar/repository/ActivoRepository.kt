package aumax.estandar.axappestandar.repository

import android.util.Log
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.remote.api.ActivoApiService
import retrofit2.Response

class ActivoRepository(
    private val tokenManager: TokenManager,
    private val activoApiService: ActivoApiService
) {
    suspend fun obtenerActivos(tagRfid: String) : Result<List<Activo>?> {
        return try {
            val response = activoApiService.obtenerActivos(tagRfid)

            if (response.isSuccessful) {
                val activos = response.body()
                Result.success(activos)
            }
            else {
                Result.failure(Exception("Error ${response}"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }
}