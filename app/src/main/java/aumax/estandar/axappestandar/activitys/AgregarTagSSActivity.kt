package aumax.estandar.axappestandar.activitys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
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
import aumax.estandar.axappestandar.data.models.Locacion.Locacion
import aumax.estandar.axappestandar.data.models.Sector.Sector
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ActivityAgregarTagSsBinding
import aumax.estandar.axappestandar.readers.AxLector
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.repository.LocacionRepository
import aumax.estandar.axappestandar.repository.SectorRepository
import aumax.estandar.axappestandar.repository.SubSectorRepository
import aumax.estandar.axappestandar.utils.TagRFID
import aumax.estandar.axappestandar.utils.adapters.SelectLocacionAdapter
import aumax.estandar.axappestandar.utils.adapters.SubSectorAdapter
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressDown
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressUp
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import kotlinx.coroutines.launch

class AgregarTagSSActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarTagSsBinding
    private lateinit var adapter: SubSectorAdapter

    private lateinit var subsectorRepository: SubSectorRepository
    private lateinit var locacionesRepository: LocacionRepository
    private lateinit var sectorRepository: SectorRepository

    private var idCompany: Int = 0

    //LISTAS
    private var locacionesList: List<Locacion> = emptyList()
    private var sectorList: List<Sector> = emptyList()
    private var subSectorList: List<SubSector> = emptyList()

    //RFID
    private var _oAxLector: AxLector? = null
    private var listenerKeyPressDown: IOnKeyPressDown? = null
    private var listenerKeyPressUp: IOnKeyPressUp? = null
    private var isReceiverRegistered = false
    private var listTagsLeidosA : MutableList<TagRFID> = ArrayList()
    private lateinit var subSectorAsignar: SubSector

    //FLAG
    private var leerTag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarTagSsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _oAxLector = AxLector(this@AgregarTagSSActivity, Configuracion.MODO_LECTURA.RFID) //creamos el objeto que va a manejar todo el RFID

        setListenerKeyPressDown(object : IOnKeyPressDown { //CONFIGURO qué hacer cuando se presione la tecla
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) { //evitar repeticiones por mantener presionado
                    if (leerTag) {
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                    }
                    else {
                        Toast.makeText(this@AgregarTagSSActivity, "Lectura no iniciada, presione un subsector para comenzar la lectura", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        setListenerKeyPressUp(object : IOnKeyPressUp {
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) {
                    _oAxLector?.DetenetLecturRFID()
                }
            }
        })

        setupRepositorys()
        setupHeaderComponent()
        setupTableComponent()
        setupListeners()
        RegistrarEventLecturaTag()

        obtenerLocaciones(idCompany)
    }

    override fun onStart() {
        super.onStart()

        if (!isReceiverRegistered ){
            val filter = IntentFilter()
            filter.addAction("android.rfid.FUN_KEY")
            // AGREGAR LA FLAG:
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)

            if(Configuracion.tipoHandheld == Configuracion.TIPO_HANDHELD.Linkwin &&
                _oAxLector?._modoLectura == Configuracion.MODO_LECTURA.Codigo ||
                _oAxLector?._modoLectura == Configuracion.MODO_LECTURA.Ambas) {
                val filter2 = IntentFilter()
                filter2.addAction("com.rfid.SCAN")
                // AGREGAR LA FLAG:
                registerReceiver(receiverCB, filter2, Context.RECEIVER_NOT_EXPORTED)
                isReceiverRegistered = true
            }
        }  //este bloque activa los detectores de hardware, como botones, para cuando se toquen se ejecute el codigo
    }

    override fun onStop() { //se ejecuta cuando al app no es visible
        super.onStop()

        if (isReceiverRegistered) {
            try {
                unregisterReceiver(receiver)
                if(Configuracion.tipoHandheld == Configuracion.TIPO_HANDHELD.Linkwin &&
                    _oAxLector?._modoLectura == Configuracion.MODO_LECTURA.Codigo ||
                    _oAxLector?._modoLectura == Configuracion.MODO_LECTURA.Ambas) {
                    unregisterReceiver(receiverCB)
                }
            } catch (e: IllegalArgumentException) {
                Log.w("Receiver", "receiver no estaba registrado")
            }
            isReceiverRegistered = false
        }// este bloque hace lo contrario al Start, 'apaga' los detectores de hardaware

    }

    private fun setListenerKeyPressDown(listener: IOnKeyPressDown) {
        this.listenerKeyPressDown = listener //aca se guardan las instrucciones que tiene que seguir cuando se hace click en una tecla
    }

    private fun setListenerKeyPressUp(listener: IOnKeyPressUp) {
        this.listenerKeyPressUp = listener //aca se guardan las instrucciones que tiene que seguir cuando se deja de presionar una tecla
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //Log.d("KEY_EVENT", "keyDown: $keyCode")
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) { //distintas teclas del RFID
            if (this.listenerKeyPressDown != null) { //aca se fija si la funcionalidad esta configurada o no
                this.listenerKeyPressDown!!.keyPress(keyCode, event)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        //Log.d("KEY_EVENT", "KeyUp: $keyCode")
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (this.listenerKeyPressUp != null) {
                this.listenerKeyPressUp!!.keyPress(keyCode, event)
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun setupRepositorys() {
        subsectorRepository = SubSectorRepository(
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
    }

    private fun setupTableComponent() {
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private var startTime: Long = 0
    var keyUpFalg = true

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var keyCode = intent.getIntExtra("keyCode", 0)
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0)
            }
            val keyDown = intent.getBooleanExtra("keydown", false)
            if (keyUpFalg && keyDown && System.currentTimeMillis() - startTime > 500) {
                keyUpFalg = false
                startTime = System.currentTimeMillis()
                if (
                    keyCode == KeyEvent.KEYCODE_F3 ||  //
                    keyCode == KeyEvent.KEYCODE_F4
                ) {
                }
                return
            } else if (keyDown) {
                startTime = System.currentTimeMillis()
            } else {
                keyUpFalg = true
                try {
                    if (listenerKeyPressDown != null) {
                        val event: KeyEvent = KeyEvent(1, 1)
                        val l = event.repeatCount
                        listenerKeyPressDown!!.keyPress(keyCode, event)
                    }
                } catch (ex: Exception) {
                    Log.i("onReceive LecturaRFIDActivity", "${ex}")
                }
            }

        }
    }

    private fun RegistrarEventLecturaTag() {
        _oAxLector!!.setListenerTagLeido(object : ITagLeidoListener { //cada vez que se detecta un evento, se dispara este callback

            override fun tagsLeidos(nuevosTags: MutableList<TagRFID>) {

            }

            override fun tagLeido(tagRFID: TagRFID) { //se ejecuta cada vez que se lee un tag

                if (leerTag) {

                    Log.d("PRUEBA LECTOR", "entro a tagA")

                    val tagExistente = listTagsLeidosA.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1 //si ya se leyo ese tag no agrega
                    } else {
                        listTagsLeidosA.add(tagRFID) //si no se leyo se agrega
                    }

                    // Si hay más de un tag leído, cancelar operación
                    if (listTagsLeidosA.size > 1) {
                        leerTag = false
                        listTagsLeidosA.clear()
                        adapter.deactivateActiveItem()

                        runOnUiThread {
                            Toast.makeText(this@AgregarTagSSActivity, "Se detectaron múltiples tags, intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }

                        Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 1")

                        return
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (listTagsLeidosA.size == 1 && leerTag) {
                            guardarTag(subSectorAsignar, listTagsLeidosA[0].TID)

                            leerTag = false
                            listTagsLeidosA.clear()
                            adapter.deactivateActiveItem()
                        }
                        else {
                            leerTag = false
                            listTagsLeidosA.clear()
                            adapter.deactivateActiveItem()

                            Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 2")

                        }
                    }, 500)

                }
            }

            override fun error(mensaje: String) {
                runOnUiThread {
                    Log.d("PRUEBA LECTOR", "${mensaje}")
                }
            }

            override fun estado(estado: Int) {

            }
        })
    }

    private val receiverCB = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getByteArrayExtra("data")
            if (data != null) {
                val barcode = String(data)
                Log.e("CB= ", barcode)
                _oAxLector?.listenerICodigoLeido?.codigoLeido(barcode)
                _oAxLector?.scanUtil?.stopScan()
                _oAxLector?.scanUtil?.close()
            } else {
                Log.e("CB= ", "ERROR EN LECTURA")
                _oAxLector?.listenerICodigoLeido?.error("No se pudo leer el código")
            }
        }
    }

    private fun setupHeaderComponent() {
        val tokenManager = MyApplication.tokenManager

        if (tokenManager.getCompanyId() != null) {
            idCompany = tokenManager.getCompanyId()!!
        }

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()
        binding.header.tvCompanyName.text = nombreEmpresa
        binding.header.tvUserName.text = username
    }

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        adapter.onAddClick = { subSector ->

            if (!leerTag) {
                if (Configuracion.potenciaRFID != 5) {
                    Log.d("CAMBIANDO POTENCIA RFID", "SE ESTA CAMBIANDO LA POTENCIA A 5")

                    _oAxLector?.DetenetLecturRFID()

                    _oAxLector?.LimpiarChainway()

                    Configuracion.potenciaRFID = 5 ///VER

                    _oAxLector?.IniciarLecturaRFID()

                }

                leerTag = true

                subSectorAsignar = subSector
            }
            else {

                leerTag = false

            }

            //Toast.makeText(this@AgregarTagSSActivity, "Leyendo Tag para ${subSector.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        adapter.submitList(subSectorList)
        progressBar.visibility = View.GONE
        emptyState.visibility = if (subSectorList.isEmpty()) View.VISIBLE else View.GONE
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

                obtenerSectores(selectedLocacion.id, idCompany, true)
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

                obtenerSubsectores(selectedSectors.id, idCompany)
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

                    Toast.makeText(this@AgregarTagSSActivity, "Error al cargar los sectores", Toast.LENGTH_SHORT).show()

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
                    Log.d("SUBSECTORES 1", "SUBSECTORES TRAIDOS ${lista}")


                    subSectorList = lista ?: emptyList()

                    Log.d("SUBSECTORES 2", "SUBSECTORES TRAIDOS ${subSectorList}")


                    setupDataOnTable()
                }
                .onFailure { error ->
                    subSectorList = emptyList()

                    Toast.makeText(this@AgregarTagSSActivity, "Error al cargar los subsectores", Toast.LENGTH_SHORT).show()
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

                    Toast.makeText(this@AgregarTagSSActivity, "Error al cargar locaciones", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarTag(subSector: SubSector, tagRFID: String) {
        lifecycleScope.launch {
            val response = subsectorRepository.asignarTagSS(tagRFID, subSector.id, idCompany)

            response
                .onSuccess {
                    Toast.makeText(this@AgregarTagSSActivity, "Tag asignado con exito", Toast.LENGTH_SHORT).show()
                    listTagsLeidosA.clear()
                    leerTag = false

                    val positionSS = subSectorList.indexOfFirst { it.id == subSector.id } //buscamos indice del actualizado

                    if (positionSS != -1) {
                        val updatedSubSector = subSectorList[positionSS].copy(tagRfid = tagRFID) //copia nueva del subsector actualizado

                        val nuevaLista = subSectorList.toMutableList()
                        nuevaLista[positionSS] = updatedSubSector

                        subSectorList = nuevaLista
                        adapter.submitList(nuevaLista)
                    }
                }

                .onFailure {
                    Toast.makeText(this@AgregarTagSSActivity, "Error al asignar TAG", Toast.LENGTH_SHORT).show()
                    Log.d("PRUEBA LECTOR", "ERROR AL ASIGNAR TAG ${response}")
                    listTagsLeidosA.clear()
                    leerTag = false
                }
        }
    }

}