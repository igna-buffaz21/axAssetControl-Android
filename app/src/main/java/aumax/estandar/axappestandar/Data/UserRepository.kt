package aumax.estandar.axappestandar.Data

/*import android.util.Log
import aumax.estandar.axappestandar.Repository.Retrofit.ApiService
import com.example.httpbasic.models.User.SyncUserRequest
import com.example.httpbasic.models.User.User
import com.example.httpbasic.models.User.UsuarioE
import com.example.httpbasic.network.ApiService
import com.example.httpbasic.room.UsuarioDAO

class UserRepository(
    private val apiService: ApiService,
    private val usuarioDAO: UsuarioDAO
) {
    suspend fun getUsers(idCompany: Int, status: String = "actived") : Result<List<User>> {
        return try {
            val response = apiService.getUsers(idCompany, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            }
            else {
                Result.failure(Exception("Error al obtener los Usuarios"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncUsers(usuarioE: List<UsuarioE>) {
        try {
            val requests = usuarioE.map { usuario -> //el map crea una lista nueva con las instrucciones que le vas dando
                SyncUserRequest(
                    idCompany = usuario.idCompany,
                    name = usuario.name,
                    email = usuario.email,
                    password = usuario.password,
                    rol = usuario.rol,
                    status = usuario.status
                )
            }

            val result = apiService.syncUserToApi(requests)

            if (result.isSuccessful) {
                Log.d("SINCRONIZACION", "${result}")

                var userIds = usuarioE.map { it.id }

                val rowsChanges = usuarioDAO.changeStatus(true , userIds)

                Log.d("SINCRONIZACION", "Se actualizaron $rowsChanges usuarios")

            }
            else {
                val errorBody = result.errorBody()?.string()
                Log.d("SINCRONIZACION", "Error HTTP ${result.code()}: $errorBody")
            }
        }
        catch (e: Exception) {
            Log.d("SINCRONIZACION", "error ${e}")
        }
    }
}*/