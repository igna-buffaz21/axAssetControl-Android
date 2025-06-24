package aumax.estandar.axappestandar.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.databinding.ListLecturaTagDataBinding
import aumax.estandar.axappestandar.utils.TagRFID

class LecturaRfidAdapter :
    ListAdapter<TagRFID, LecturaRfidAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ListLecturaTagDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListLecturaTagDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        with(holder.binding) {
            txtEpcTid.text = "EPC: ${data.EPC}\nTID: ${data.TID}"
            txtCount.text = data.Count.toString()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TagRFID>() {
        override fun areItemsTheSame(oldItem: TagRFID, newItem: TagRFID): Boolean {
            return oldItem.TID == newItem.TID // Identificador único
        }

        override fun areContentsTheSame(oldItem: TagRFID, newItem: TagRFID): Boolean {
            return oldItem == newItem // Compara Count, EPC, etc.
        }

    }
}
