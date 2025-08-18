package aumax.estandar.axappestandar.activitys

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.databinding.DashboardBinding

class DashboardActivity(
) : AppCompatActivity() {

    private lateinit var binding: DashboardBinding //lateinit indica que se inicializa despues
    private val tokenManager = MyApplication.tokenManager

    //private lateinit var viewModel:

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DashboardBinding.inflate(layoutInflater) ///crea objetos del XML
        setContentView(binding.root)


        setupListeners() //escuchan acciones del usuario
    }

    private fun setupListeners() {

        binding.btnAbm.setOnClickListener {
            navigateToABM()
        }
        binding.btnControl.setOnClickListener {
            navigateToControl()
        }
        binding.btnReasignarActivo.setOnClickListener {
            navigateToReasign()
        }
        binding.btnLogout.setOnClickListener {
            logOut()
        }
    }

    private fun navigateToABM() {

        val intent = Intent(this, SeleccionABMActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToControl() {

        val intent = Intent(this, SeleccionControlActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToReasign() {
        val intent = Intent(this, ReasignarActivoActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun logOut() {
        tokenManager.clearToken()
        tokenManager.clearAll()

        val username = tokenManager.obtenerNombreUsuario()
        val token = tokenManager.getToken()

        Log.d("BORRADO DE SHARED", "username ${username}")
        Log.d("BORRADO DE SHARED", "token ${token}")

        if (tokenManager.getToken() == "" || tokenManager.getToken() == null) {

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }
    }
}