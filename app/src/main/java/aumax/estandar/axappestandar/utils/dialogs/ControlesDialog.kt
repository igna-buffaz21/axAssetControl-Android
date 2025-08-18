package aumax.estandar.axappestandar.utils.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerControlLocalDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerDetalleControlLocalDTO
import aumax.estandar.axappestandar.databinding.LayoutModalActivosBinding
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.RegistroControlRepository
import aumax.estandar.axappestandar.utils.adapters.ActivosAdapter
import aumax.estandar.axappestandar.utils.adapters.DetalleControlBDAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ControlesDialog(
    context: Context,
    private val obtenerControlLocalDTO: ObtenerControlLocalDTO,
    private val registroControlRepository: RegistroControlRepository
) : Dialog(context), CoroutineScope {

    private lateinit var adapter: DetalleControlBDAdapter
    private lateinit var binding: LayoutModalActivosBinding
    private var controlList: List<ObtenerDetalleControlLocalDTO> = emptyList()

    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job

    override fun dismiss() {
        super.dismiss()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Usar binding correctamente
        binding = LayoutModalActivosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DetalleControlBDAdapter()

        setupWindow()
        setupViews()
        setupRecyclerView()
        loadControl()
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
        binding.tvModalTitle.text = "Control en ${obtenerControlLocalDTO.name}"

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
        binding.rvActivos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ControlesDialog.adapter

            // Mejorar scroll performance
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }
    }

    private fun loadControl() {
        showLoading(true)
        launch {
            try {
                val response = registroControlRepository.obtenerDetallesDeControlBD(obtenerControlLocalDTO.id)

                response
                    .onSuccess { lista ->
                        controlList = lista ?: emptyList()
                        updateUI()
                    }
                    .onFailure { error ->
                        controlList = emptyList()
                        updateUI()
                        showError("Error al cargar activos: ${error.message}")
                    }

            } catch (e: Exception) {
                controlList = emptyList()
                updateUI()
                showError("Error inesperado: ${e.message}")
            }
        }
    }

    private fun updateUI() {
        showLoading(false)
        adapter.submitList(controlList)

        // Manejar estado vacío con el nuevo contenedor
        binding.emptyStateContainer.visibility =
            if (controlList.isEmpty()) View.VISIBLE else View.GONE
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

    companion object {
        /**
         * Factory method para crear y mostrar el modal
         */
        fun show(
            context: Context,
            obtenerControlLocalDTO: ObtenerControlLocalDTO,
            registroControlRepository: RegistroControlRepository
        ) {
            ControlesDialog(context, obtenerControlLocalDTO, registroControlRepository).show()
        }
    }
}