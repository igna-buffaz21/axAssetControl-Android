package aumax.estandar.axappestandar.utils.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.local.entities.SubSector
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.databinding.ItemActivoBinding
import aumax.estandar.axappestandar.databinding.LayoutModalActivosBinding
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.utils.adapters.ActivosAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ReasignarActivoDialog(
    context: Context,
    private val subSector: aumax.estandar.axappestandar.data.models.SubSector.SubSector,
    private val activosRepository: ActivoRepository
) : Dialog(context), CoroutineScope {

    private lateinit var adapter: ActivosAdapter
    private lateinit var binding: LayoutModalActivosBinding
    private var activosList: List<Active> = emptyList()

    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job

    override fun dismiss() {
        super.dismiss()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar dialog sin título
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Usar binding correctamente
        binding = LayoutModalActivosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar ventana para el modal moderno
        setupWindow()
        setupViews()
        setupRecyclerView()
        loadActivos()
    }

    private fun setupWindow() {
        window?.apply {
            // Hacer que ocupe toda la pantalla con fondo transparente
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // Agregar animaciones suaves (opcional)
            setWindowAnimations(android.R.style.Animation_Dialog)
        }
    }

    private fun setupViews() {
        // Configurar título y nombre del subsector
        binding.tvModalTitle.text = subSector.name

        // Configurar botón cerrar
        binding.btnCloseModal.setOnClickListener {
            dismiss()
        }

        // Configurar click en el fondo para cerrar modal
        binding.modalBackground.setOnClickListener {
            dismiss()
        }

        // Evitar que se cierre al hacer click en el contenido del modal
        binding.modalCard.setOnClickListener {
            // No hacer nada para evitar que se propague el click
        }
    }

    private fun setupRecyclerView() {
        adapter = ActivosAdapter { activo ->
            onActivoClicked(activo)
        }

        binding.rvActivos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ReasignarActivoDialog.adapter

            // Mejorar scroll performance
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }
    }

    private fun loadActivos() {
        showLoading(true)
        launch {
            try {
                val response = activosRepository.obtenerActivosBD(subSector.id)

                response
                    .onSuccess { lista ->
                        activosList = lista ?: emptyList()
                        updateUI()
                    }
                    .onFailure { error ->
                        activosList = emptyList()
                        updateUI()
                        showError("Error al cargar activos: ${error.message}")
                    }

            } catch (e: Exception) {
                activosList = emptyList()
                updateUI()
                showError("Error inesperado: ${e.message}")
            }
        }
    }

    private fun updateUI() {
        showLoading(false)
        adapter.submitList(activosList)

        // Manejar estado vacío con el nuevo contenedor
        binding.emptyStateContainer.visibility = if (activosList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showLoading(show: Boolean) {
        binding.apply {
            if (show) {
                progressBarActivos.visibility = View.VISIBLE
                rvActivos.visibility = View.GONE
                emptyStateContainer.visibility = View.GONE
            } else {
                progressBarActivos.visibility = View.GONE
                rvActivos.visibility = View.VISIBLE
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun onActivoClicked(activo: Active) {
        // Manejar click en activo
        // Por ejemplo: abrir detalle del activo, seleccionar, etc.
        Toast.makeText(context, "Activo seleccionado: ${activo.name}", Toast.LENGTH_SHORT).show()
        // dismiss() // Si quieres cerrar el modal al seleccionar
    }

    companion object {
        /**
         * Factory method para crear y mostrar el modal
         */
        fun show(
            context: Context,
            subSector: aumax.estandar.axappestandar.data.models.SubSector.SubSector,
            activosRepository: ActivoRepository
        ) {
            ModalActivosDialog(context, subSector, activosRepository).show()
        }
    }
}