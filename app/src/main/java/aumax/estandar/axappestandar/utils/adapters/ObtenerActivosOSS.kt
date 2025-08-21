package aumax.estandar.axappestandar.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.Activos.ObtenerActivosOSSDTO
import aumax.estandar.axappestandar.databinding.ItemTablaControlBinding

class ObtenerActivosOSS :
    ListAdapter<ObtenerActivosOSSDTO, ObtenerActivosOSS.ViewHolder>(DiffCallback()) {//SubSector (tipo de dato para mostrar), SubSectorAdapter.ViewHolder (es el tipo de ViewHolder que vas a usar para mostrar cada ítem), DiffCallback() (es la lógica para comparar si los ítems cambiaron.)

    var onAddClick: ((Activo) -> Unit)? = null

    class ViewHolder(val binding: ItemTablaControlBinding) : //item dentro de la tabla
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder { //crea la vista vacia //se llama solo si no hay vistas para reciclar
        val binding = ItemTablaControlBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //aca asignamos los datos a las vistas // se llama cada vez que un item se va a mostrar
        val activo = getItem(position)
        with(holder.binding) {
            tvName.text = activo.name + " / "
            tvTag.text = activo.idSubsectorNavigation.name
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ObtenerActivosOSSDTO>() {
        override fun areItemsTheSame(oldItem: ObtenerActivosOSSDTO, newItem: ObtenerActivosOSSDTO): Boolean { //son el mismo objeto logico?
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ObtenerActivosOSSDTO, newItem: ObtenerActivosOSSDTO): Boolean { //tienen exactamente lo mismo dato? no -> se ejecuta onBindViewHolder
            return oldItem == newItem
        }
    }
}