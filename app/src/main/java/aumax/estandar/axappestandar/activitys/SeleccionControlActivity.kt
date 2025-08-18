package aumax.estandar.axappestandar.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.databinding.ActivitySeleccionControlBinding

class SeleccionControlActivity(

) : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionControlBinding //lateinit indica que se inicializa despues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionControlBinding.inflate(layoutInflater) ///crea objetos del XML
        setContentView(binding.root)

        setupListeners()
        setupTableComponent()
    }

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.controlOff.setOnClickListener {
            navigateToControlOff()
        }
        binding.controlOn.setOnClickListener {
            navigateToControlOn()
        }
        binding.configuracionOff.setOnClickListener {
            navigateToConfigOff()
        }
    }

    private fun setupTableComponent() {
        val tokenManager = MyApplication.tokenManager

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()

        binding.header.tvUserName.text = username

        binding.header.tvCompanyName.text = nombreEmpresa
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

    private fun navigateToConfigOff() {
        val intent = Intent(this, SeleccionConfiguracionOffActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

}