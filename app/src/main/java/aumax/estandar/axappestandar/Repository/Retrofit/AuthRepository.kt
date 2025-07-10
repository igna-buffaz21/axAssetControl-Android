package aumax.estandar.axappestandar.Repository.Retrofit

import android.util.Log
import aumax.estandar.axappestandar.Data.TokenManager
import aumax.estandar.axappestandar.Models.LoginRequest

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                tokenManager.saveToken(token)
                val tokenSave = tokenManager.getToken()
                Log.d("Token Guardado en Shared Preference", "$tokenSave")
                Result.success(token)
            }
            else {
                Log.d("Token Guardado", "Error al iniciar sesion")
                Result.failure(Exception("Login failed"))
            }
        }
        catch (e: Exception) {
            Log.e("Token Guardado", "Excepción en login: ${e.message}")
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
}