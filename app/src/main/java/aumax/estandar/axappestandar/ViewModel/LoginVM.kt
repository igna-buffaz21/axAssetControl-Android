package aumax.estandar.axappestandar.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aumax.estandar.axappestandar.Repository.Retrofit.AuthRepository
import kotlinx.coroutines.launch

class LoginVM(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(usuario: String, password: String) {

        var validacion = validarLogin(usuario, password)

        if (validacion) {
            viewModelScope.launch {
                try {
                    val result = authRepository.login(usuario, password)

                    if (result.isSuccess) {
                        result.onSuccess { token ->
                            Log.d("Token Guardado", "Login exitoso con token: $token")
                            _loginSuccess.value = true  // ✅ Esto activa la navegación
                        }
                    } else {
                        result.onFailure { exception ->
                            Log.d("Token Guardado", "Login fallido: ${exception.message}")
                            _errorMessage.value = exception.message ?: "Error desconocido"
                        }
                    }
                }
                catch (e: Exception) {
                    Log.d("Token Guardado", "Catch ViewModel: ${e.message}")
                    Result.failure<String>(e)
                }

            }
        }
    }

    private fun validarLogin(usuario: String, password: String) : Boolean {
        if (usuario.isBlank()) {
            _errorMessage.value = "El usuario es requerido"
            _loginSuccess.value = false
            return false
        }

        if (usuario.length < 3) {
            _errorMessage.value = "El usuario debe tener al menos 3 caracteres"
            _loginSuccess.value = false
            return false
        }

        if (password.isBlank()) {
            _errorMessage.value = "La contraseña es requerida"
            _loginSuccess.value = false
            return false
        }

        if (password.length < 4) {
            _errorMessage.value = "La contraseña debe tener al menos 4 caracteres"
            _loginSuccess.value = false
            return false
        }

        return true

    }

}