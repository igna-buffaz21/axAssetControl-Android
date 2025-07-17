package aumax.estandar.axappestandar.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.databinding.ActivitySeleccionAbmBinding

class SeleccionABMActivity(

) : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionAbmBinding //lateinit indica que se inicializa despues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySeleccionAbmBinding.inflate(layoutInflater) ///crea objetos del XML
        setContentView(binding.root)


        setupListeners()
        setupTableComponent()
    }

    private fun setupListeners() {
        binding.btnAgregarTagSs.setOnClickListener {
            navigateToAgregarTagSSActivity()
        }
        binding.btnAgregarTagA.setOnClickListener {
            navigateToAgregarTagAActivity()
        }
    }

    private fun setupTableComponent() {
        val tokenManager = MyApplication.tokenManager

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()

        binding.header.tvUserName.text = username

        binding.header.tvCompanyName.text = nombreEmpresa
    }


    private fun navigateToAgregarTagSSActivity() {
        val intent = Intent(this, AgregarTagSSActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToAgregarTagAActivity() {
        val intent = Intent(this, AgregarTagAActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

}