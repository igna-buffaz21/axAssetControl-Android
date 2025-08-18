package aumax.estandar.axappestandar.utils.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ItemTablaBinding
import aumax.estandar.axappestandar.databinding.ItemTablaDescargarBinding

class DescargarSubSectorAdapter :
    ListAdapter<SubSector, DescargarSubSectorAdapter.ViewHolder>(DiffCallback()) {

    var onAddClick: ((SubSector) -> Unit)? = null

    // Guarda la posición del ítem activo (si hay alguno)
    private var activePosition: Int? = null

    // Guarda los IDs de los subsectores descargados exitosamente
    private val downloadedSubSectors = mutableSetOf<Int>() // o String según el tipo de tu ID

    inner class ViewHolder(val binding: ItemTablaDescargarBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTablaDescargarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subSector = getItem(position)

        with(holder.binding) {
            tvName.text = subSector.name
            tvTag.text = subSector.tagRfid

            // Prioridad de íconos: descargado > activo > normal
            btnAdd.setImageResource(
                when {
                    downloadedSubSectors.contains(subSector.id) -> R.drawable.ic_check // ✅ Descargado
                    position == activePosition -> R.drawable.ic_download // 📖 Descargando
                    else -> R.drawable.ic_down // ➕ Sin descargar
                }
            )

            // Si ya está descargado, deshabilitar el click
            btnAdd.isEnabled = !downloadedSubSectors.contains(subSector.id)

            // Opcional: cambiar la opacidad si está descargado
            btnAdd.alpha = if (downloadedSubSectors.contains(subSector.id)) 0.5f else 1.0f

            btnAdd.setOnClickListener {
                val currentPosition = holder.adapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                // No hacer nada si ya está descargado
                if (downloadedSubSectors.contains(subSector.id)) return@setOnClickListener

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

    /** 🔹 Marcar un subsector como descargado exitosamente */
    fun markAsDownloaded(subSectorId: Int) { // Cambia a String si tu ID es String
        downloadedSubSectors.add(subSectorId)

        // Desactivar el ítem activo si es el mismo que se descargó
        activePosition?.let { position ->
            val activeSubSector = getItem(position)
            if (activeSubSector.id == subSectorId) {
                activePosition = null
            }
        }

        // Actualizar la vista del ítem específico
        findPositionById(subSectorId)?.let { position ->
            notifyItemChanged(position)
        }
    }

    /** 🔹 Marcar múltiples subsectores como descargados */
    fun markMultipleAsDownloaded(subSectorIds: List<Int>) {
        downloadedSubSectors.addAll(subSectorIds)
        notifyDataSetChanged() // Si son muchos, es más eficiente
    }

    /** 🔹 Resetear todos los subsectores descargados */
    fun clearDownloaded() {
        downloadedSubSectors.clear()
        notifyDataSetChanged()
    }

    /** 🔹 Verificar si un subsector está descargado */
    fun isDownloaded(subSectorId: Int): Boolean {
        return downloadedSubSectors.contains(subSectorId)
    }

    /** 🔹 Obtener la lista de IDs descargados */
    fun getDownloadedIds(): Set<Int> {
        return downloadedSubSectors.toSet()
    }

    /** 🔹 Función auxiliar para encontrar la posición de un ítem por ID */
    private fun findPositionById(subSectorId: Int): Int? {
        for (i in 0 until itemCount) {
            if (getItem(i).id == subSectorId) {
                return i
            }
        }
        return null
    }
}