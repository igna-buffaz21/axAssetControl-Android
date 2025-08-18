package aumax.estandar.axappestandar.repository

import android.content.Context
import android.util.Log
import aumax.estandar.axappestandar.data.local.DataBaseProvider
import aumax.estandar.axappestandar.data.local.entities.ControlRecord
import aumax.estandar.axappestandar.data.local.entities.DetailControl
import aumax.estandar.axappestandar.data.models.Activos.ResponseAsignarTagActivo
import aumax.estandar.axappestandar.data.models.RegistroControl.CrearDetalleControlDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerControlLocalDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerDetalleControlLocalDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.RegistroControlDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.SincronizacionDTO
import aumax.estandar.axappestandar.data.remote.api.RegistroControlApiService
import retrofit2.Response

class RegistroControlRepository(
    private val registroControlApiService: RegistroControlApiService,
    private val context: Context
) {

    private val db = DataBaseProvider.getDataBase(context)
    private  val registroControlDAO = db.registroControlDao()

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

    suspend fun crearDetalleControlenCantidad(crearDetallesdeControl: List<CrearDetalleControlDTO>) : Result<String?> {
        return try {
            val response = registroControlApiService.CrearDetallesControlEnCantidad(crearDetallesdeControl)

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

    suspend fun crearControlBD(id_subsector: Int, date: Long, id_company: Int) : Result<Long> {
        return try {
            val control = ControlRecord(
                id = 0,
                id_subsector,
                date,
                "Completed",
                id_company,
                false
            )

            val response = registroControlDAO.crearControl(control)

            if (response != -1L) {
                Result.success(response)
            }
            else {
                Result.failure(Exception("Error al crear el control"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearDetalleControlBD(detalledeControl: List<DetailControl>) : Result<String> {
        return try {
            val response = registroControlDAO.crearDetalleControl(detalledeControl)

            if (response.all { it != -1L }) {
                Result.success("Detalles de control creados con exito")
            }
            else {
                Result.failure(Exception("Error al crear detalles de control"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerControlBD(id_company: Int) : Result<List<ObtenerControlLocalDTO>> {
        return try {
            val response = registroControlDAO.obtenerControles(id_company)

            if (response.size > 0) {
                Result.success(response)
            }
            else {
                Result.failure(Exception("Error al obtener los controles ${response}"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerDetallesDeControlBD(id_control: Int) :
            Result<List<ObtenerDetalleControlLocalDTO>> {
        return try {
            val response = registroControlDAO.obtenerDetalleControlPorControl(id_control)

            if (response.size > 0) {
                Result.success(response)
            }
            else {
                Result.failure(Exception("Error al obtener los detalles de control ${response}"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarControlesConAPI(controles: List<ObtenerControlLocalDTO>,
                                           detalles: List<ObtenerDetalleControlLocalDTO>) : Result<Response<ResponseAsignarTagActivo>>
    {
        return try {
            val request = SincronizacionDTO(
                controles = controles,
                detalles = detalles
            )

            Log.d("DATOS A ENVIAR A LA API", "${request}")

            val response = registroControlApiService.SincronizarControlesYDetalles(request)

            if (response.isSuccessful) {
                Result.success(response)
            }
            else {
                Result.failure(Exception("Error al sincronizar"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun marcarComoAsync() {
        try {
            registroControlDAO.marcarAsyncControl()
            registroControlDAO.marcarAsyncDetailControl()
        }
        catch (e: Exception) {
            Log.d("Error al marcar como async", "${e}")
        }
    }

}