package aumax.estandar.axappestandar.activitys

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ActivityAgregarTagABinding
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.SubSectorRepository
import aumax.estandar.axappestandar.utils.adapters.ActivoAdapter
import aumax.estandar.axappestandar.utils.adapters.SelectLocacionAdapter
import aumax.estandar.axappestandar.utils.adapters.SubSectorAdapter
import kotlinx.coroutines.launch


class AgregarTagAActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarTagABinding
    private lateinit var adapter: ActivoAdapter

    private lateinit var activoRepository: ActivoRepository

    private var activoList: List<Activo> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarTagABinding.inflate(layoutInflater)
        setContentView(binding.root)

        activoRepository = ActivoRepository( //reutilizamos las instacias creadas al inicio de la aplicacion
            MyApplication.tokenManager,
            MyApplication.activoApiService
        )

        setupTableComponent()
        RegistrarEventLecturaTag() //configurar
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

        adapter = ActivoAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        adapter.submitList(activoList)
        progressBar.visibility = View.GONE
        emptyState.visibility = if (activoList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun RegistrarEventLecturaTag() {
        ////RFID

        val tagRfid = "12134"
        obtenerActivos(tagRfid)
    }

    private fun obtenerActivos(tagRfid: String) {
        lifecycleScope.launch {
            val response = activoRepository.obtenerActivos(tagRfid)
            response
                .onSuccess { lista ->

                    activoList = lista ?: emptyList()

                    Log.d("ACTIVOS", "SECTORES TRAIDOS ${activoList}")

                    setupDataOnTable()
                }
                .onFailure { error ->

                    activoList = emptyList()

                    Log.d("ERROR AL OBTENER LOS ACTIVOS", "ERROR: ${response}")

                    Toast.makeText(this@AgregarTagAActivity, "Error al cargar los activos", Toast.LENGTH_SHORT).show()
                }
        }
    }

}