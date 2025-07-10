package aumax.estandar.axappestandar.Repository.Retrofit

import android.util.Log
import aumax.estandar.axappestandar.Data.TokenManager
import aumax.estandar.axappestandar.Models.User
import retrofit2.Response

class UsersRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun getUsers() : Response<List<User>>? {
        return try {
            val idCompany = tokenManager.getCompanyId()

            Log.e("Token Guardado", "id: ${idCompany}")

            if (idCompany != null) {
                apiService.getUsers(idCompany, "actived")
            } else {
                Log.e("Token Guardado", "Company ID is null")
                null
            }

        } catch (e: Exception) {
            Log.e("Token Guardado", "Error al obtener usuarios", e)
            null
        }
    }
}