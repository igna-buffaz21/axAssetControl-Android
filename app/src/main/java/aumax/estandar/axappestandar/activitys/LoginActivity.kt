package aumax.estandar.axappestandar.activitys

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.repository.AuthRepository
import aumax.estandar.axappestandar.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding //lateinit indica que se inicializa despues
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater) ///crea objetos del XML
        setContentView(binding.root)

        authRepository = AuthRepository( //reutilizamos las instacias creadas al inicio de la aplicacion
            MyApplication.userApiService,
            MyApplication.tokenManager
        )

        setupListeners() //escuchan acciones del usuario
    }

    private fun setupListeners() {
        binding.iniciarSesion.setOnClickListener {
            val usuario = binding.usuario.editText?.text.toString().trim()
            val password = binding.password.editText?.text.toString().trim()
            if (validarDatos(usuario, password)) {
                iniciarSesion(usuario, password)
            }
        }
    }

    private fun iniciarSesion(usuario: String, password: String) {
        showLoaging(true)
        lifecycleScope.launch {
                val response = authRepository.login(usuario, password)
                if (response.isSuccess) {
                    showLoaging(false)
                    Toast.makeText(this@LoginActivity, "Login Exitoso", Toast.LENGTH_SHORT).show()
                    navigateToDashboardActivity()
                }
                else {
                    showLoaging(false)
                    Toast.makeText(this@LoginActivity, "Error al iniciar sesion", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validarDatos(usuario: String, password: String): Boolean {
        var valido = true

        // Validar email
        if (usuario.isEmpty()) {
            binding.usuario.error = "El correo es obligatorio"
            valido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(usuario).matches()) {
            binding.usuario.error = "Correo inválido"
            valido = false
        } else {
            binding.usuario.error = null
        }

        // Validar contraseña
        if (password.isEmpty()) {
            binding.password.error = "La contraseña es obligatoria"
            valido = false
        } else if (password.length < 6) {
            binding.password.error = "Mínimo 6 caracteres"
            valido = false
        } else {
            binding.password.error = null
        }

        return valido
    }

    private fun showLoaging(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
            binding.iniciarSesion.text = " "
            binding.iniciarSesion.isEnabled = false
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.iniciarSesion.text = "Iniciar Sesión"
            binding.iniciarSesion.isEnabled = true
        }
    }

    private fun navigateToDashboardActivity() {

        val intent = Intent(this, DashboardActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        finish() //finaliza esta actividad
    }

}

