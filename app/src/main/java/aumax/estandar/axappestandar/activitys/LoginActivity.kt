package aumax.estandar.axappestandar.activitys

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import aumax.estandar.axappestandar.Data.TokenManager
import aumax.estandar.axappestandar.MyApplication
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.Repository.Retrofit.AuthRepository
import aumax.estandar.axappestandar.Repository.Retrofit.RetrofitClient
import aumax.estandar.axappestandar.ViewModel.LoginVM
import aumax.estandar.axappestandar.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding //lateinit indica que se inicializa despues
    private lateinit var viewModel: LoginVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater) ///crea objetos del XML
        setContentView(binding.root)

        val authRepository = AuthRepository( //reutilizamos las instacias creadas al inicio de la aplicacion
            MyApplication.apiService,
            MyApplication.tokenManager
        )

        viewModel = LoginVM(authRepository) //inicializamos la variable

        setupObservers() //escuchan cambios en los datos
        setupListeners() //escuchan acciones del usuario
    }

    private fun setupListeners() {
        binding.iniciarSesion.setOnClickListener {
            val usuario = binding.usuario.editText?.text.toString().trim()
            val password = binding.password.editText?.text.toString().trim()
            viewModel.login(usuario, password)
        }
    }

    private fun setupObservers() {
        viewModel.loginSuccess.observe(this) { success -> //escucha al viewmodel, loginsucces y succes es el valor recibido
            if (success) { //si es true entra y navega y da un mensaje
                Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                navigateToDashboardActivity()
            }
        }

        viewModel.errorMessage.observe(this) { error -> //aca tambien escucha a errorMessage
            // Limpiar errores previos
            binding.usuario.error = null
            binding.password.error = null

            if (error != null) { //en caso de que sea distinto a null:
                val errorText = error
                if (errorText.contains("usuario", true)) {
                    binding.usuario.error = errorText
                } else if (errorText.contains("contraseña", true)) {
                    binding.password.error = errorText
                } else {
                    Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun navigateToDashboardActivity() {

        val intent = Intent(this, DashboardActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        finish() //finaliza esta actividad
    }

}

