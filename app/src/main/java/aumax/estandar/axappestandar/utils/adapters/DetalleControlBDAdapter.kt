package aumax.estandar.axappestandar.utils.adapters

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerDetalleControlLocalDTO
import aumax.estandar.axappestandar.databinding.ItemActivoBinding


class DetalleControlBDAdapter(
    private val onActivoClick: (ObtenerDetalleControlLocalDTO) -> Unit = {}
) : ListAdapter<ObtenerDetalleControlLocalDTO, DetalleControlBDAdapter.ActivoViewHolder>(ActivoDiffCallback()) {

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

        fun bind(activo: ObtenerDetalleControlLocalDTO) {
            binding.apply {
                // Campos principales
                tvActivoNombre.text = activo.name

                if (activo.status == "available") {
                    val background = tvActivoCodigo.background as GradientDrawable
                    background.setColor(ContextCompat.getColor(root.context, R.color.disponible))
                }
                else {
                    val background = tvActivoCodigo.background as GradientDrawable
                    background.setColor(ContextCompat.getColor(root.context, R.color.noDisponible))
                }
            }
        }
    }

    class ActivoDiffCallback : DiffUtil.ItemCallback<ObtenerDetalleControlLocalDTO>() {
        override fun areItemsTheSame(
            oldItem: ObtenerDetalleControlLocalDTO,
            newItem: ObtenerDetalleControlLocalDTO
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ObtenerDetalleControlLocalDTO,
            newItem: ObtenerDetalleControlLocalDTO
        ): Boolean {
            return oldItem == newItem
        }
    }
}