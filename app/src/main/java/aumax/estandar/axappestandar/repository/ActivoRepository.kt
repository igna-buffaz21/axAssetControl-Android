package aumax.estandar.axappestandar.repository

import android.content.Context
import android.util.Log
import aumax.estandar.axappestandar.data.local.DataBaseProvider
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.local.dao.ActivoDAO
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.Activos.ObtenerActivosOSSDTO
import aumax.estandar.axappestandar.data.models.Activos.ResponseAsignarTagActivo
import aumax.estandar.axappestandar.data.models.Activos.RetornarActivosDTOySubsectorDTO
import aumax.estandar.axappestandar.data.remote.api.ActivoApiService
import org.json.JSONObject
import retrofit2.Response

class ActivoRepository(
    private val tokenManager: TokenManager,
    private val activoApiService: ActivoApiService,
    private val context: Context
) {

    private val db = DataBaseProvider.getDataBase(context)
    private val activoDAO = db.activoDao()

    suspend fun obtenerActivos(tagRfid: String, idCompany: Int): Result<RetornarActivosDTOySubsectorDTO?> {
        return try {
            val response = activoApiService.obtenerActivos(tagRfid, idCompany)

            if (response.isSuccessful) {
                val activos = response.body()
                Result.success(activos)
            } else {
                Result.failure(Exception("Error ${response}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerActivos(idSubSector: Int, idEmpresa: Int, status: Boolean): List<Activo>? {
        return try {
            val response =
                activoApiService.obtenerActivoConIdSubSector(idSubSector, idEmpresa, status)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun obtenerActivosBD(idSubSector: Int): Result<List<Active>> {
        return try {
            val response = activoDAO.obtenerTodos(idSubSector)

            if (response.size > 0) {
                Result.success(response)
            } else {
                Result.failure(Exception("Error al obtener los activos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asigarTagRfidActivo(
        Rfid: String,
        idActivo: Int,
        idEmpresa: Int
    ): Result<Response<ResponseAsignarTagActivo>> {
        return try {
            val response = activoApiService.asigarTagActivo(Rfid, idActivo, idEmpresa)

            if (response.isSuccessful) {
                Result.success(response)
            } else {
                val mensajeError = try {
                    val errorJson = response.errorBody()?.string()
                    JSONObject(errorJson).getString("mensaje")
                } catch (e: Exception) {
                    "Error desconocido al asignar tag RFID"
                }

                Result.failure(Exception("Error al asignar tag RFID: $mensajeError"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerActivosPorRfid(tagRfid: String, idEmpresa: Int): Result<Activo?> {
        return try {
            val response = activoApiService.obtenerActivoPorRfid(tagRfid, idEmpresa)

            if (response.isSuccessful) {
                val activos = response.body()
                Result.success(activos)
            } else {
                Result.failure(Exception("Error ${response}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reasignarActivo(
        idActivo: Int,
        idSubSector: Int
    ): Result<Response<ResponseAsignarTagActivo>> {
        return try {
            val response = activoApiService.reasignarTagRfidDeLugar(idActivo, idSubSector)

            if (response.isSuccessful) {
                Result.success(response)
            } else {
                Result.failure(Exception("Error ${response}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerActivosPorRfidBD(tagRfid: String, idEmpresa: Int): Result<List<Active>> {
        return try {
            val id_subsector = activoDAO.obtenerIdSubsector(tagRfid, idEmpresa)

            Log.d("RESPUESTA DE DAO", "${id_subsector}")

            if (id_subsector != 0) {

                val activos = activoDAO.obtenerActivosBD(id_subsector)

                if (activos.size > 0) {
                    Result.success(activos)
                } else {
                    Result.failure(Exception("Error al encontrar los activos"))
                }
            } else {
                Result.failure(Exception("Error al encontrar el id del subsector"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerTodosLosActivosEmpresa(idCompany: Int) : Result<List<ObtenerActivosOSSDTO>?> {
        return try {
            val activos = activoApiService.obtenerTodosLosActivoEmpresa(idCompany)

            if (activos.isSuccessful) {

                val activos = activos.body()
                Result.success(activos)

            } else {
                Result.failure(Exception("Error al obtener los activos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerNombreSubSector(tagRfid: String, idCompany: Int) : Result<String?> {
        return try {
            val response = activoDAO.obtenerNombreSubSector(tagRfid, idCompany)

            if (response != "") {
                Result.success(response)
            }
            else {
                Result.failure(Exception("Error al obtener el nombre del subsector"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }
}