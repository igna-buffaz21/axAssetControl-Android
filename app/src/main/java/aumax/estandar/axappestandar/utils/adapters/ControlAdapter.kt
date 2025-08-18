package aumax.estandar.axappestandar.utils.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerControlLocalDTO
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ItemTablaBinding

class ControlAdapter :
    ListAdapter<ObtenerControlLocalDTO, ControlAdapter.ViewHolder>(DiffCallback()) {

    var onAddClick: ((ObtenerControlLocalDTO) -> Unit)? = null

    // Guarda la posición del ítem activo (si hay alguno)
    private var activePosition: Int? = null

    inner class ViewHolder(val binding: ItemTablaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTablaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val control = getItem(position)

        with(holder.binding) {
            tvName.text = control.name
            tvTag.text = control.date.toString()

            btnAdd.setImageResource(
                R.drawable.ic_arrow_see
            )

            btnAdd.setOnClickListener {
                onAddClick?.invoke(control)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ObtenerControlLocalDTO>() {
        override fun areItemsTheSame(oldItem: ObtenerControlLocalDTO, newItem: ObtenerControlLocalDTO) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ObtenerControlLocalDTO, newItem: ObtenerControlLocalDTO) =
            oldItem == newItem
    }
}
