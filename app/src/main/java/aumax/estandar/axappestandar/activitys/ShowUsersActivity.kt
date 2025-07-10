package aumax.estandar.axappestandar.activitys

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.Adapters.UsersAdapter
import aumax.estandar.axappestandar.Models.User
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.Repository.Retrofit.AuthRepository
import aumax.estandar.axappestandar.Repository.Retrofit.UsersRepository
import aumax.estandar.axappestandar.ViewModel.LoginVM
import aumax.estandar.axappestandar.ViewModel.ShowUsersVM
import aumax.estandar.axappestandar.databinding.ItemUserCardBinding
import aumax.estandar.axappestandar.databinding.ShowUsersBinding


class ShowUsersActivity : AppCompatActivity() {
    private lateinit var binding: ItemUserCardBinding
    private lateinit var usersAdapter: UsersAdapter
    private val usersList = mutableListOf<User>()
    private lateinit var viewModel: ShowUsersVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ItemUserCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usersRepository = UsersRepository( //reutilizamos las instacias creadas al inicio de la aplicacion
            MyApplication.apiService,
            MyApplication.tokenManager
        )

        viewModel = ShowUsersVM(usersRepository) //inicializamos la variable

        setupRecyclerView()
        observeViewModel()

        viewModel.getUsers()
    }

    private fun setupRecyclerView() {
        usersAdapter = UsersAdapter()

        binding.recyclerUsers.apply {
            adapter = usersAdapter
            layoutManager = LinearLayoutManager(this@ShowUsersActivity)
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(this) { users ->
            usersAdapter.submitList(users)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}