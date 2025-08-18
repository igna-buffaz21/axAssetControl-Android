package aumax.estandar.axappestandar.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ItemTablaBinding

class SubSectorAdapter :
    ListAdapter<SubSector, SubSectorAdapter.ViewHolder>(DiffCallback()) {

    var onAddClick: ((SubSector) -> Unit)? = null

    // Guarda la posición del ítem activo (si hay alguno)
    private var activePosition: Int? = null

    inner class ViewHolder(val binding: ItemTablaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTablaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subSector = getItem(position)

        with(holder.binding) {
            tvName.text = subSector.name
            tvTag.text = subSector.tagRfid

            // Cambia icono según si este ítem es el activo
            btnAdd.setImageResource(
                if (position == activePosition) R.drawable.ic_reader else R.drawable.ic_add
            )

            btnAdd.setOnClickListener {
                val currentPosition = holder.adapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                if (activePosition == currentPosition) {
                    // Si ya estaba activo, lo desactivo
                    val oldPosition = activePosition
                    activePosition = null
                    oldPosition?.let { notifyItemChanged(it) }
                } else {
                    // Desactivo el que estaba activo antes
                    activePosition?.let { notifyItemChanged(it) }

                    // Activo este
                    activePosition = currentPosition
                    notifyItemChanged(currentPosition)

                    // Aviso a la Activity
                    onAddClick?.invoke(subSector)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SubSector>() {
        override fun areItemsTheSame(oldItem: SubSector, newItem: SubSector) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SubSector, newItem: SubSector) =
            oldItem == newItem
    }

    /** 🔹 Llamar desde la Activity para desactivar automáticamente el ítem activo */
    fun deactivateActiveItem() {
        activePosition?.let {
            val oldPos = it
            activePosition = null
            notifyItemChanged(oldPos)
        }
    }
}
