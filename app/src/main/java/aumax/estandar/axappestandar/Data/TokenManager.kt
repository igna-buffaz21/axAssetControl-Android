package aumax.estandar.axappestandar.Data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.io.encoding.ExperimentalEncodingApi

class TokenManager(context: Context) { ///context seria como la referancia de la app/actividad
    private val sharedPreferences: SharedPreferences = ///aca creamos como una instancia del localstorage
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE) //nombre del local y permisos

    companion object {
        private const val TOKEN_KEY = "jwt_token" ///se usa el mismo para TODAS las instancias de la clase
    }

    fun saveToken(token: String) {
        sharedPreferences.edit() ///inicio en modo edicion
            .putString(TOKEN_KEY, token) ///guarda el string
            .apply() ///guarda cambios async
    }

    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        sharedPreferences.edit() ///inicio en modo edicion
            .remove(TOKEN_KEY) ///borra todo en la clave
            .apply() ///async
    }

    fun hasToken(): Boolean {
        return getToken() != null
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun getTokenClaims(): Map<String, Any>? {
        val token = getToken() ?: return null
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val jsonString = String(decodedBytes)

            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson<Map<String, Any>>(jsonString, type)
        }
        catch (e: Exception) {
            Log.e("TokenManager", "Error decoding token: ${e.message}")
            null
        }
    }

    fun getCompanyId() : Int? {
        return getTokenClaims()?.get("companyId")?.toString()?.toIntOrNull()
    }
}