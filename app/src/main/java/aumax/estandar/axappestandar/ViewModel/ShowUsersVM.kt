package aumax.estandar.axappestandar.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aumax.estandar.axappestandar.Models.User
import aumax.estandar.axappestandar.Repository.Retrofit.UsersRepository
import kotlinx.coroutines.launch

class ShowUsersVM(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getUsers() { // Cambiar a public
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = usersRepository.getUsers()

                if (result?.isSuccessful == true) {
                    _users.value = result.body() ?: emptyList()
                } else {
                    _error.value = "Error al cargar usuarios: ${result?.code()}"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
                Log.e("ShowUsersVM", "Error al obtener usuarios", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}