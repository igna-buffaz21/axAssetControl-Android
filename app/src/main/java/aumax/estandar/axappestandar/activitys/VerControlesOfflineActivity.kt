package aumax.estandar.axappestandar.activitys

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerControlLocalDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerDetalleControlLocalDTO
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ActivityVerSsDescargadosBinding
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.RegistroControlRepository
import aumax.estandar.axappestandar.repository.SubSectorRepository
import aumax.estandar.axappestandar.utils.adapters.ControlAdapter
import aumax.estandar.axappestandar.utils.adapters.VerSubSectorAdapter
import aumax.estandar.axappestandar.utils.dialogs.ControlesDialog
import aumax.estandar.axappestandar.utils.dialogs.ModalActivosDialog
import kotlinx.coroutines.launch

class VerControlesOfflineActivity(

) : AppCompatActivity() {

    private lateinit var binding: ActivityVerSsDescargadosBinding
    private lateinit var adapter: ControlAdapter
    private var subSectorList: List<SubSector> = emptyList()
    private lateinit var registroControlRepository: RegistroControlRepository
    private var controlList: MutableList<ObtenerControlLocalDTO> = mutableListOf()

    private var detalleList: MutableList<ObtenerDetalleControlLocalDTO> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerSsDescargadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRepositorys()
        setupTableComponent()
        setupListeners()

        val tokenManager = MyApplication.tokenManager

        val idCompany = tokenManager.getCompanyId()

        if (idCompany != null) {
            obtenerControles(idCompany)
        }
        else {
            Toast.makeText(this@VerControlesOfflineActivity, "Error inesperado, reinicie la aplicacion", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sincronizar() {
        lifecycleScope.launch  {
            val response = registroControlRepository.sincronizarControlesConAPI(controlList, detalleList)

                response
                    .onSuccess {
                        Toast.makeText(this@VerControlesOfflineActivity, "Sincronizacion con exito", Toast.LENGTH_SHORT).show()

                        asignarAsync()

                        controlList.clear()
                        detalleList.clear()

                        setupDataOnTable()
                    }
                    .onFailure {
                        Toast.makeText(this@VerControlesOfflineActivity, "Error al sincronizar", Toast.LENGTH_SHORT).show()
                    }
        }
    }

    private fun asignarAsync() {
        lifecycleScope.launch  {
            registroControlRepository.marcarComoAsync()
        }
    }

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSincronizar.setOnClickListener {
            if (controlList.size > 0 && detalleList.size > 0) {
                sincronizar()
            }
            else {
                Toast.makeText(this@VerControlesOfflineActivity, "No hay controles a sincronizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTableComponent() {

        binding.tvTableTitle.text = "Controles a Sincronizar"

        val tokenManager = MyApplication.tokenManager

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()
        binding.header.tvCompanyName.text = nombreEmpresa
        binding.header.tvUserName.text = username

        val recyclerView = binding.tableComponent.recyclerTable
        val emptyState = binding.tableComponent.emptyState
        val progressBar = binding.tableComponent.progressBar
        val headerLayout = binding.tableComponent.tableHeader

        headerLayout.removeAllViews()

        listOf("Nombre", "Fecha", "Ver").forEach { title ->
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                text = title
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerLayout.addView(tv)
        }

        adapter = ControlAdapter()

        adapter.onAddClick = { control ->
            Log.d("INFORMACION DEL CONTROL", "info: ${control}")

            lifecycleScope.launch {
                val modal = ControlesDialog(
                    this@VerControlesOfflineActivity,
                    control,
                    registroControlRepository
                )
                modal.show()
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupRepositorys() {
        registroControlRepository = RegistroControlRepository(
            MyApplication.registroControlApiService,
            context = this
        )
    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        adapter.submitList(controlList)
        progressBar.visibility = View.GONE
        emptyState.visibility = if (controlList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun obtenerControles(id_company: Int) {
        lifecycleScope.launch  {
            val response = registroControlRepository.obtenerControlBD(id_company)

            response
                .onSuccess { controles ->

                    controlList.addAll(controles)

                    Log.d("INFORMACION DEL CONTROL", "info: ${controles}")

                    setupDataOnTable()

                    loadControl()
                }
                .onFailure { error ->
                    Toast.makeText(this@VerControlesOfflineActivity, "Error al obtener los Controles ${error}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadControl() {
        lifecycleScope.launch  {
            try {
                controlList.forEach { control ->
                    val response = registroControlRepository.obtenerDetallesDeControlBD(control.id)
                    response
                        .onSuccess { lista ->
                            detalleList.addAll(lista)
                        }
                        .onFailure { error ->
                            Log.d("ERROR AL OBTENER LOS DETALLES", "info: ${error}")
                        }
                }

                Log.d("DETALLES OBTENIDOS DESDE LA ACTIVITY", "info: ${detalleList}")

            } catch (e: Exception) {
                Log.d("ERROR AL OBTENER LOS DETALLES", "info: ${e}")
            }
        }
    }
}