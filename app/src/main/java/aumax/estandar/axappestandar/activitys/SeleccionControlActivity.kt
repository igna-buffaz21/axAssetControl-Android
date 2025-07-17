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
        binding.descargarSs.setOnClickListener {
            navigateToDescargarSS()
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
}