package aumax.estandar.axappestandar.repository

import android.util.Log
import aumax.estandar.axappestandar.data.models.Activos.ResponseAsignarTagActivo
import aumax.estandar.axappestandar.data.models.RegistroControl.CrearDetalleControlDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.RegistroControlDTO
import aumax.estandar.axappestandar.data.remote.api.RegistroControlApiService
import retrofit2.Response

class RegistroControlRepository(
    private val registroControlApiService: RegistroControlApiService
) {

    suspend fun guardarRegistroControl(idSubsector: Int, date: Long, idCompany: Int) : Result<Int?> {
        return try {
            val registroControlDTO = RegistroControlDTO(
            idSubsector,
            date,
            idCompany
            )

            val response = registroControlApiService.registrarControl(registroControlDTO)

            if (response.isSuccessful) {
                Log.d("ENTRO EN SUCCESSFUL", "${response}")
                Result.success(response.body()?.idRegistroControl)
            }
            else {
                Log.d("ENTRO EN SUCCESSFUL ELSE", "${response}")
                Result.failure(Exception("error ${response}"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearDetalleControl(idControl: Int, idActivo: Int, status: String, idAuditor: Int) : Result<String?> {
        return try {
            val crearDetalleControl = CrearDetalleControlDTO(
                idControl,
                idActivo,
                status,
                idAuditor
            )

            val response = registroControlApiService.crearDetalleControl(crearDetalleControl)

            if (response.isSuccessful) {
                Log.d("ENTRO EN  de DETALLE CONTROL", "${response}")
                Result.success(response.body()?.mensaje)
            }
            else {
                Log.d("ENTRO EN de DETALLE CONTROL", "${response}")
                Result.failure(Exception("error al crear"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

}