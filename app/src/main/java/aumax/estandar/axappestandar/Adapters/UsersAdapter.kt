package aumax.estandar.axappestandar.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.Models.User
import aumax.estandar.axappestandar.databinding.ShowUsersBinding

class UsersAdapter : ListAdapter<User, UsersAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ShowUsersBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ShowUsersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = getItem(position)
        with(holder.binding) {
            tvUserName.text = user.name
            tvUserRole.text = user.rol
            tvUserEmail.text = user.email

            // Obtener la inicial del nombre
            val initial = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            tvUserInitial.text = initial
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.email == newItem.email // Email como identificador único
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem // Compara todos los campos
        }
    }
}