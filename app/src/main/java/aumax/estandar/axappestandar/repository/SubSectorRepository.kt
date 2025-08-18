package aumax.estandar.axappestandar.repository

import android.content.Context
import android.util.Log
import aumax.estandar.axappestandar.data.local.AppDataBase
import aumax.estandar.axappestandar.data.local.DataBaseProvider
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.SubSector.ResponseAsignarTagSS
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.data.remote.api.SubSectorApiService
import aumax.estandar.axappestandar.data.toEntity
import aumax.estandar.axappestandar.data.toModel
import retrofit2.Response

class SubSectorRepository(
    private val tokenManager: TokenManager,
    private val subSectorApiService: SubSectorApiService,
    private val context: Context
) {

    private val db = DataBaseProvider.getDataBase(context)
    private  val subSectorDAO = db.subsectorDao()

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

    suspend fun insertarSubSectorYActivos(subSector: aumax.estandar.axappestandar.data.local.entities.SubSector, activos: List<Active>) : Result<String> {
        return try {
            val response = subSectorDAO.descargarSubsectorYActivo(subSector, activos)

            if (response) {
                Result.success("Descarga con exito")
            }
            else {
                Result.failure(Exception("Error al Descargar, intentelo de nuevo"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerSubsectoresBD(idCompany: Int) : Result<List<SubSector>> {
        return try {
            val response = subSectorDAO.obtenerTodos(idCompany)

            if (response.size > 0) {
                val subSectorBD = response.map { it.toModel() }

                Result.success(subSectorBD)
            }
            else {
                Result.failure(Exception("Error al obtener los subsectores"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarTagSS(Rfid: String, idSubsector: Int, idEmpresa: Int) : Result<Response<ResponseAsignarTagSS>> {
        return try {
            val response = subSectorApiService.asignarTagRfid(Rfid, idSubsector, idEmpresa )

            if (response.isSuccessful) {
                Result.success(response)
            }
            else {
                Result.failure(Exception("Error al asignarle un tag al subsector"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerSectoresPorRfid(tagRfid: String, idEmpresa: Int) : Result<SubSector?> {
        return try {
            val response = subSectorApiService.obtenerSubSectorPorRfid(tagRfid, idEmpresa)

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