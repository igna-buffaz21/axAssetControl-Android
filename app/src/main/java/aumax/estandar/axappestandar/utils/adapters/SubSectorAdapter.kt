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
    ListAdapter<SubSector,SubSectorAdapter.ViewHolder>(DiffCallback()) { //SubSector (tipo de dato para mostrar), SubSectorAdapter.ViewHolder (es el tipo de ViewHolder que vas a usar para mostrar cada ítem), DiffCallback() (es la lógica para comparar si los ítems cambiaron.)

    var onAddClick: ((SubSector) -> Unit)? = null
    private var leerTag: Boolean = false

    class ViewHolder(val binding: ItemTablaBinding) : //item dentro de la tabla
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //crea la vista vacia //se llama solo si no hay vistas para reciclar
        val binding = ItemTablaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //aca asignamos los datos a las vistas // se llama cada vez que un item se va a mostrar
        val subSector = getItem(position)
        with(holder.binding) {
            tvName.text = subSector.name
            tvTag.text = subSector.tagRfid
            //tvStatus.text = if (subSector.status) "Activo" else "Inactivo"
            btnAdd.setOnClickListener {
                if (!leerTag) {
                    leerTag = true

                    holder.binding.btnAdd.setImageResource(
                        R.drawable.ic_reader
                    )

                    onAddClick?.invoke(subSector)
                }
                else {
                    leerTag = false

                    holder.binding.btnAdd.setImageResource(
                        R.drawable.ic_add
                    )
                }
            }

        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SubSector>() {
        override fun areItemsTheSame(oldItem: SubSector, newItem: SubSector): Boolean { //son el mismo objeto logico?
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubSector, newItem: SubSector): Boolean { //tienen exactamente lo mismo dato? no -> se ejecuta onBindViewHolder
            return oldItem == newItem
        }
    }
}