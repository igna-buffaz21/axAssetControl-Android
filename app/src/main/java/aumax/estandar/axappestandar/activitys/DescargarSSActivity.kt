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
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.Locacion.Locacion
import aumax.estandar.axappestandar.data.models.Sector.Sector
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.data.toEntity
import aumax.estandar.axappestandar.databinding.ActivityDescargarSubsectoresBinding
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.LocacionRepository
import aumax.estandar.axappestandar.repository.SectorRepository
import aumax.estandar.axappestandar.repository.SubSectorRepository
import aumax.estandar.axappestandar.utils.adapters.DescargarSubSectorAdapter
import aumax.estandar.axappestandar.utils.adapters.SelectLocacionAdapter
import aumax.estandar.axappestandar.utils.adapters.SubSectorAdapter
import kotlinx.coroutines.launch

class DescargarSSActivity(

) : AppCompatActivity() {

    private lateinit var binding: ActivityDescargarSubsectoresBinding
    private lateinit var subsectorRepository: SubSectorRepository
    private lateinit var locacionesRepository: LocacionRepository
    private lateinit var sectorRepository: SectorRepository
    private lateinit var activoRepository: ActivoRepository

    private var locacionesList: List<Locacion> = emptyList()
    private var sectorList: List<Sector> = emptyList()
    private var subSectorList: List<SubSector> = emptyList()
    private var activoList: List<Activo> = emptyList()
    private var idEmpresa: Int = 0

    private lateinit var adapter: DescargarSubSectorAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDescargarSubsectoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTableComponent()
        setupRepositorys()
        idEmpresa = MyApplication.tokenManager.getCompanyId()!!

        binding.header.btnBack.setOnClickListener {
            finish()
        }

        if (idEmpresa != 0) {
            obtenerLocaciones(idEmpresa)
        }
        else {
            Toast.makeText(this@DescargarSSActivity, "Error Inesperado, reinicie la aplicacion", Toast.LENGTH_SHORT).show()
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

        listOf("Nombre", "Tag Rfid", "Descargar").forEach { title ->
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                text = title
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerLayout.addView(tv)
        }

        adapter = DescargarSubSectorAdapter()

// En tu configuración del adapter
        adapter.onAddClick = { subSector ->
            Log.d("INFORMACION DEL SUBSECTOR", "info: ${subSector}")

            lifecycleScope.launch {
                val activos = obtenerActivos(subSector.id, subSector.idEmpresa, true) ?: emptyList()

                val activoDB = activos.map { it.toEntity() }
                val subSectorDB = subSector.toEntity()

                Log.d("INFO ANTES DE CONSULTA", "info SS: ${subSectorDB}")
                Log.d("INFO ANTES DE CONSULTA", "info ACT: ${activoDB}")

                // Pasar el subSector original para poder marcarlo como descargado
                descargarSubsectorYActivo(subSectorDB, activoDB, subSector)
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupRepositorys() {
        subsectorRepository = SubSectorRepository( //reutilizamos las instacias creadas al inicio de la aplicacion
            MyApplication.tokenManager,
            MyApplication.subSectorApiService,
            context = this
        )

        locacionesRepository = LocacionRepository(
            MyApplication.tokenManager,
            MyApplication.locacionApiService
        )

        sectorRepository = SectorRepository(
            MyApplication.tokenManager,
            MyApplication.sectorApiService
        )

        activoRepository = ActivoRepository(
            MyApplication.tokenManager,
            MyApplication.activoApiService,
            this
        )
    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        adapter.submitList(subSectorList)
        progressBar.visibility = View.GONE
        emptyState.visibility = if (subSectorList.isEmpty()) View.VISIBLE else View.GONE
    }

    private suspend fun obtenerActivos(idSubSector: Int, idEmpresa: Int, status: Boolean): List<Activo>? {
        return activoRepository.obtenerActivos(idSubSector, idEmpresa, status)
    }

    private fun setupLocationSpinner() {
        val locations = locacionesList.map { it.name }

        val adapter = SelectLocacionAdapter(this, locations)
        binding.spinnerLocation.setAdapter(adapter)

        binding.spinnerLocation.setOnClickListener {
            binding.spinnerLocation.showDropDown()
        }

        binding.spinnerLocation.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedLocation = locations[position]
                val selectedLocacion = locacionesList[position]

                binding.spinnerLocation.setText(selectedLocation, false)
                binding.spinnerLocation.dismissDropDown()
                binding.spinnerLocation.clearFocus()

                // RESETEAR EL SELECTOR DE SECTORES
                resetSectorSpinner()

                obtenerSectores(selectedLocacion.id, idEmpresa, true)
            }
    }

    private fun setupSectorSpinner() {
        val sectors = sectorList.map { it.name }

        val adapter = SelectLocacionAdapter(this, sectors)
        binding.spinnerSector.setAdapter(adapter)

        binding.spinnerSector.setOnClickListener {
            binding.spinnerSector.showDropDown()
        }

        binding.spinnerSector.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedSector = sectors[position]
                val selectedSectors = sectorList[position]

                binding.spinnerSector.setText(selectedSector, false)
                binding.spinnerSector.dismissDropDown()
                binding.spinnerSector.clearFocus()

                Log.d("SectorSpinner", "Seleccionado: ${selectedSectors.name}")
                Log.d("SectorSpinner", "ID: ${selectedSectors.id}")
                Log.d("SectorSpinner", "Otras propiedades: ${selectedSectors}")

                obtenerSubsectores(selectedSectors.id, idEmpresa)
            }
    }

    private fun resetSectorSpinner() {
        binding.spinnerSector.setText("", false)

        sectorList = emptyList()

        subSectorList = emptyList()

        val emptyAdapter = SelectLocacionAdapter(this, emptyList())
        binding.spinnerSector.setAdapter(emptyAdapter)

        setupDataOnTable()
    }

    private fun obtenerSectores(idLocacion: Int, idEmpresa: Int, status: Boolean) {
        lifecycleScope.launch {
            val response = sectorRepository.obtenerSectores(idLocacion, idEmpresa, status)
            response
                .onSuccess { lista ->
                    sectorList = lista ?: emptyList()

                    Log.d("SECTOR", "SECTORES TRAIDOS ${sectorList}")

                    setupSectorSpinner()
                }
                .onFailure { error ->
                    sectorList = emptyList()

                    Toast.makeText(this@DescargarSSActivity, "Error al cargar los sectores", Toast.LENGTH_SHORT).show()

                    // En caso de error, también resetear el spinner
                    resetSectorSpinner()
                }
        }
    }

    private fun obtenerSubsectores(idSector: Int, idEmpresa: Int) {
        lifecycleScope.launch {
            val response = subsectorRepository.obtenerSectores(idSector, idEmpresa)
            response
                .onSuccess { lista ->
                    subSectorList = lista ?: emptyList()

                    setupDataOnTable()
                }
                .onFailure { error ->
                    subSectorList = emptyList()

                    Toast.makeText(this@DescargarSSActivity, "Error al cargar los subsectores", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun obtenerLocaciones(idEmpresa: Int) {
        lifecycleScope.launch {
            val response = locacionesRepository.obtenerLocaciones(idEmpresa, true)
            response
                .onSuccess { lista ->
                    locacionesList = lista ?: emptyList()

                    setupLocationSpinner()
                }
                .onFailure { error ->
                    locacionesList = emptyList()

                    Toast.makeText(this@DescargarSSActivity, "Error al cargar locaciones", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Función modificada para incluir el subSector original
    private fun descargarSubsectorYActivo(
        subSector: aumax.estandar.axappestandar.data.local.entities.SubSector,
        activos: List<Active>,
        originalSubSector: SubSector // Agregar este parámetro
    ) {
        lifecycleScope.launch {
            val response = subsectorRepository.insertarSubSectorYActivos(subSector, activos)
            response
                .onSuccess { exito ->
                    // ✅ Éxito - marcar como descargado
                    adapter.markAsDownloaded(originalSubSector.id)
                    Toast.makeText(this@DescargarSSActivity, "${exito}", Toast.LENGTH_SHORT).show()
                }
                .onFailure { error ->
                    // ❌ Error - desactivar ítem activo
                    adapter.deactivateActiveItem()
                    Toast.makeText(this@DescargarSSActivity, "Error al Descargar los activos", Toast.LENGTH_SHORT).show()
                    Log.d("ERROR AL OBTENER ACTIVOS", "${error}")
                }
        }
    }

}