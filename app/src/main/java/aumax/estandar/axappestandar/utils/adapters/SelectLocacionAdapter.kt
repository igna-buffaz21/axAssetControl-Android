package aumax.estandar.axappestandar.utils.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.Locacion.Locacion
import aumax.estandar.axappestandar.databinding.ItemSelectBinding

class SelectLocacionAdapter(
    context: Context,
    private val location: List<String>
) : ArrayAdapter<String>(context, 0, location) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemSelectBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemSelectBinding.bind(convertView)
        }

        val location = location[position]
        binding.txtLocation.text = location

        // Cambiar icono según la posición

        return binding.root
    }
}