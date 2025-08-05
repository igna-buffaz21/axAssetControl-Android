package aumax.estandar.axappestandar.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.databinding.ItemActivoBinding

class ActivosAdapter(
    private val onActivoClick: (Active) -> Unit = {}
) : ListAdapter<Active, ActivosAdapter.ActivoViewHolder>(ActivoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivoViewHolder {
        val binding = ItemActivoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActivoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ActivoViewHolder(
        private val binding: ItemActivoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onActivoClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(activo: Active) {
            binding.apply {
                // Campos principales
                tvActivoNombre.text = activo.name
                tvActivoCodigo.text = activo.tag_rfid ?: "N/A"

                // Manejar campo Marca
                if (!activo.brand.isNullOrBlank()) {
                    layoutMarca.visibility = View.VISIBLE
                    tvActivoMarca.text = activo.brand
                } else {
                    layoutMarca.visibility = View.GONE
                }

                // Si ni marca ni versión están disponibles, ocultar todo el contenedor
                val hasAdditionalInfo = !activo.brand.isNullOrBlank() || !activo.version.toString().isNullOrBlank()
                // Asumiendo que el LinearLayout padre de marca y versión tiene un ID
                // Si no lo tienes, puedes agregarlo o manejar la visibilidad individualmente
            }
        }
    }

    class ActivoDiffCallback : DiffUtil.ItemCallback<Active>() {
        override fun areItemsTheSame(oldItem: Active, newItem: Active): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Active, newItem: Active): Boolean {
            return oldItem == newItem
        }
    }
}