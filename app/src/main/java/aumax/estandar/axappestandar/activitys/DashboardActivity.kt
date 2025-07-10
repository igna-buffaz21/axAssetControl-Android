package aumax.estandar.axappestandar.activitys

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import aumax.estandar.axappestandar.Data.TokenManager
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.MyApplication.Companion.tokenManager
import aumax.estandar.axappestandar.Repository.Retrofit.UsersRepository
import aumax.estandar.axappestandar.ViewModel.LoginVM
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
        binding.btnHome.setOnClickListener {
            navigateToRFIDActivity()
        }
        binding.btnUsers.setOnClickListener {
            navigateToUsers()
        }
        binding.btnLogout.setOnClickListener {
            logOut()
        }
    }

    private fun navigateToUsers() {
        val intent = Intent(this, ShowUsersActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun navigateToRFIDActivity() {

        val intent = Intent(this, LecturaRFIDActivity::class.java)

        startActivity(intent) //inicia la otra actividad

        //finish() //finaliza esta actividad
    }

    private fun logOut() {
        tokenManager.clearToken()
        if (tokenManager.getToken() == "" || tokenManager.getToken() == null) {

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)

            finish()
        }
    }
}