package aumax.estandar.axappestandar.activitys

import android.graphics.Typeface
import android.os.Bundle
import android.os.PersistableBundle
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
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.data.toEntity
import aumax.estandar.axappestandar.databinding.ActivityVerSsDescargadosBinding
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.SubSectorRepository
import aumax.estandar.axappestandar.utils.adapters.SubSectorAdapter
// aumax.estandar.axappestandar.utils.dialogs.ModalActivosDialog
import kotlinx.coroutines.launch

class VerSSDActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityVerSsDescargadosBinding
    private lateinit var adapter: SubSectorAdapter
    private var subSectorList: List<SubSector> = emptyList()
    private lateinit var subsectorRepository: SubSectorRepository
    private lateinit var activoRepository: ActivoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerSsDescargadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subsectorRepository = SubSectorRepository( //reutilizamos las instacias creadas al inicio de la aplicacion
            MyApplication.tokenManager,
            MyApplication.subSectorApiService,
            context = this
        )

        activoRepository = ActivoRepository(
            MyApplication.tokenManager,
            MyApplication.activoApiService,
            this
        )


        setupTableComponent()

        val tokenManager = MyApplication.tokenManager
        val idCompany = tokenManager.getCompanyId()

        if (idCompany != null) {
            obtenerSubsectores(idCompany)
        }
        else {
            Toast.makeText(this@VerSSDActivity, "Error inesperado, reinicie la aplicacion", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTableComponent() {

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

        listOf("Nombre", "Tag RFID", "Agregar Tag").forEach { title ->
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                text = title
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerLayout.addView(tv)
        }

        adapter = SubSectorAdapter()

        adapter.onAddClick = { subSector ->
            Log.d("INFORMACION DEL SUBSECTOR", "info: ${subSector}")

            /*lifecycleScope.launch {
                val modal = ModalActivosDialog(
                    this@VerSSDActivity,
                    subSector,
                    activoRepository
                )
                modal.show()
            }*/
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        adapter.submitList(subSectorList)
        progressBar.visibility = View.GONE
        emptyState.visibility = if (subSectorList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun obtenerSubsectores(idEmpresa: Int) {
        lifecycleScope.launch {
            val response = subsectorRepository.obtenerSubsectoresBD(idEmpresa)
                response
                .onSuccess { lista ->

                    subSectorList = lista ?: emptyList()

                    Log.d("INFORMACION DEL SUBSECTOR", "info: ${lista}")

                    setupDataOnTable()
                }
                .onFailure { error ->
                    subSectorList = emptyList()

                    Toast.makeText(this@VerSSDActivity, "Error al cargar los subsectores", Toast.LENGTH_SHORT).show()
                }
        }
    }
}