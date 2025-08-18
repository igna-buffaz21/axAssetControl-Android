package aumax.estandar.axappestandar.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.databinding.ActivitySeleccionConfiguracionOffBinding
import aumax.estandar.axappestandar.databinding.ActivitySeleccionControlBinding

class SeleccionConfiguracionOffActivity(

) : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionConfiguracionOffBinding //lateinit indica que se inicializa despues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionConfiguracionOffBinding.inflate(layoutInflater) ///crea objetos del XML
        setContentView(binding.root)

        setupListeners()
        setupTableComponent()
    }

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.descargarSs.setOnClickListener {
            navigateToDescargarSS()
        }
        binding.verSsDescargados.setOnClickListener {
            navigateToVerSS()
        }
        binding.verControles.setOnClickListener {
            navigateToVerControlesOff()
        }
    }

    private fun setupTableComponent() {
        val tokenManager = MyApplication.tokenManager

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()

        binding.header.tvUserName.text = username

        binding.header.tvCompanyName.text = nombreEmpresa
    }

    private fun navigateToDescargarSS() {
        val intent = Intent(this, DescargarSSActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToVerSS() {
        val intent = Intent(this, VerSSDActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToVerControlesOff() {
        val intent = Intent(this, VerControlesOfflineActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToControlOn() {
        val intent = Intent(this, ControlOnlineActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToControlOff() {
        val intent = Intent(this, ControlOfflineActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

}