package aumax.estandar.axappestandar.repository

import android.util.Log
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.models.User.LoginRequest
import aumax.estandar.axappestandar.data.remote.api.UserApiService

class AuthRepository(
    private val userApiService: UserApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val loginRequest = LoginRequest(email, password)
            val response = userApiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!.token
                tokenManager.saveToken(token)

                val claims = tokenManager.getTokenClaims()
                val userId = claims?.get("userId")?.toString()?.toIntOrNull()

                obtenerDatos(userId)

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

    suspend fun obtenerDatos(userId: Int?) {

        if (userId == null) {
            Log.e("API", "Token Invalido")
            return
        }

        try {
            val responseU = userApiService.obtenerDatosUsuario(userId)

            if (responseU.isSuccessful) {
                val datosUsuario = responseU.body()

                if (datosUsuario != null) {
                    tokenManager.guardarDatosUsuario(datosUsuario)
                }

                Log.e("API", "Usuario: ${datosUsuario}")
            }
            else {
                Log.e("API", "Error")
            }

        }
        catch (e: Exception) {
            Log.e("API", "Error: ${e}")
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }
}