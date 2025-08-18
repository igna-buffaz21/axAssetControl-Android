package aumax.estandar.axappestandar.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.databinding.ActivitySeleccionAbmBinding

class SeleccionABMActivity(

) : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionAbmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionAbmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupTableComponent()
    }

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAgregarTagSs.setOnClickListener {
            navigateToAgregarTagSSActivity()
        }
        binding.btnAgregarTagA.setOnClickListener {
            navigateToAgregarTagAActivity()
        }
        binding.btnReasignarActivo.setOnClickListener {
            navigateToReasignarTagSSActivity()
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

    private fun navigateToReasignarTagSSActivity() {
        val intent = Intent(this, ReasignarTagActivoActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToAgregarTagAActivity() {
        val intent = Intent(this, AgregarTagAActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

}