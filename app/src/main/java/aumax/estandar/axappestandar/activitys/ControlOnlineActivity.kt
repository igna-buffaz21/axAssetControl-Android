package aumax.estandar.axappestandar.activitys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.Activos.ObtenerActivoDTO
import aumax.estandar.axappestandar.data.models.Activos.ObtenerActivosOSSDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.CrearDetalleControlDTO
import aumax.estandar.axappestandar.databinding.ActivityControlOnlineBinding
import aumax.estandar.axappestandar.readers.AxLector
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.RegistroControlRepository
import aumax.estandar.axappestandar.utils.TagRFID
import aumax.estandar.axappestandar.utils.adapters.ActivoControlAdapter
import aumax.estandar.axappestandar.utils.dialogs.ActivosEncontradosSSDialog
import aumax.estandar.axappestandar.utils.dialogs.ModalActivosDialog
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressDown
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressUp
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.time.Instant
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlOnlineActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityControlOnlineBinding
    private lateinit var adapter: ActivoControlAdapter

    private lateinit var activoRepository: ActivoRepository
    private lateinit var registroControlRepository: RegistroControlRepository

    private var activoList: MutableList<Activo> = ArrayList()

    private var activosCompanyList: MutableList<ObtenerActivosOSSDTO> = ArrayList()

    private var activoListEncontrados: MutableList<Activo> = ArrayList()
    private var activoListEncontradosdeOtroSector: MutableList<ObtenerActivosOSSDTO> = ArrayList()
    private var idSubsectorControl: Int = 0

    private var subSectorName: String = ""

    private var isControlActive = false

    private var idCompany: Int = 0

    private var idUsuario: Int = 0

    //RFID
    private var _oAxLector: AxLector? = null
    private var listenerKeyPressDown: IOnKeyPressDown? = null
    private var listenerKeyPressUp: IOnKeyPressUp? = null
    private var isReceiverRegistered = false
    private var listTagsLeidos : MutableList<TagRFID> = ArrayList()
    private var listTagsLeidosControl : MutableList<TagRFID> = ArrayList()

    //FLAG
    private var leerTagSS: Boolean = false
    private var leerTagsControl: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityControlOnlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _oAxLector = AxLector(this@ControlOnlineActivity, Configuracion.MODO_LECTURA.RFID) //creamos el objeto que va a manejar todo el RFID

        setListenerKeyPressDown(object : IOnKeyPressDown { //CONFIGURO qué hacer cuando se presione la tecla
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) { //evitar repeticiones por mantener presionado
                    if (leerTagSS && !leerTagsControl) {
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                        Log.d("ESTADO LECTURA", "SE INICIO LA LECTURA SS")
                    }
                    else if (leerTagsControl && !leerTagSS) {
                        Log.d("ESTADO LECTURA", "SE INICIO LA LECTURA C")
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                    }
                    else {
                        when {
                            !leerTagSS && !leerTagsControl -> {
                                Toast.makeText(this@ControlOnlineActivity, "Lectura no iniciada, Lectores desactivados", Toast.LENGTH_SHORT).show()
                            }
                            !leerTagSS -> {
                                Toast.makeText(this@ControlOnlineActivity, "Lectura no iniciada, Presione en Leer Tag Subsector", Toast.LENGTH_SHORT).show()
                            }
                            !leerTagsControl -> {
                                Toast.makeText(this@ControlOnlineActivity, "Lectura no iniciada, Presione en Iniciar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        })

        setListenerKeyPressUp(object : IOnKeyPressUp {
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) {
                    Log.d("ESTADO LECTURA", "SE DETUVO LA LECTURA")
                    _oAxLector?.DetenetLecturRFID()
                }
            }
        })


        setupHeaderComponent()
        setupRepositorys()
        setupTableComponent()
        setupListeners()
        RegistrarEventLecturaTag()

        obtenerTodosLosActivosEmpresa(idCompany)
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

    private fun setupTableComponent() {
        val recyclerView = binding.tableComponent.recyclerTable
        val emptyState = binding.tableComponent.emptyState
        val progressBar = binding.tableComponent.progressBar
        val headerLayout = binding.tableComponent.tableHeader

        headerLayout.removeAllViews()

        listOf("Nombre", "Tag RFID").forEach { title ->
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
                text = title
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            headerLayout.addView(tv)
        }

        adapter = ActivoControlAdapter()
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

            override fun tagLeido(tagRFID: TagRFID) {

                if (leerTagSS) {
                    val tagExistente = listTagsLeidos.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1 //si ya se leyo ese tag no agrega
                    }
                    else {
                        listTagsLeidos.add(tagRFID) //si no se leyo se agrega
                    }

                    // Si hay más de un tag leído, cancelar operación
                    if (listTagsLeidos.size > 1) {
                        leerTagSS = false
                        listTagsLeidos.clear()

                        binding.btnLeerTagSubsector.setBackgroundColor(
                            ContextCompat.getColor(this@ControlOnlineActivity, R.color.black)
                        )

                        binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                        runOnUiThread {
                            Toast.makeText(this@ControlOnlineActivity, "Se detectaron múltiples tags, intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }

                        Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 1")

                        return
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (listTagsLeidos.size == 1 && leerTagSS) {
                            obtenerActivos(listTagsLeidos[0].TID)
                            listTagsLeidos.clear()
                            leerTagSS = false

                            binding.btnLeerTagSubsector.setBackgroundColor(
                                ContextCompat.getColor(this@ControlOnlineActivity, R.color.black)
                            )

                            binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                        }
                        else {
                            Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 2")
                            listTagsLeidos.clear()
                            leerTagSS = false

                            binding.btnLeerTagSubsector.setBackgroundColor(
                                ContextCompat.getColor(this@ControlOnlineActivity, R.color.black)
                            )

                            binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                        }
                    }, 500) // Espera 500ms para ver si se suma otro tag
                }

                else if (leerTagsControl) {
                    val tagExistente = listTagsLeidosControl.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1
                    } else {
                        listTagsLeidosControl.add(tagRFID)

                        val epcsLeidos = listTagsLeidosControl.map { it.TID }.toSet()

                        // ✅ Recorremos la lista maestra, sin borrar objetos
                        for (activo in activoList) {
                            if (activo.tagRfid in epcsLeidos) {
                                if (!activoListEncontrados.contains(activo)) {
                                    Log.d("SE ENCONTRO UN ACTIVO", activo.name)
                                    activo.encontrado = "available"
                                    activoListEncontrados.add(activo)
                                }
                            }
                        }

                        val listaFaltantes = activoList.filter { it.encontrado != "available" }

                        adapter.submitList(listaFaltantes)
                    }
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

        if (tokenManager.getCompanyId() != null && tokenManager.obtenerIdUsuario() != null) {
            idCompany = tokenManager.getCompanyId()!!
            idUsuario = tokenManager.obtenerIdUsuario()!!
        }

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()
        binding.header.tvCompanyName.text = nombreEmpresa
        binding.header.tvUserName.text = username
    }

    private fun setupRepositorys() {
        activoRepository = ActivoRepository(
            MyApplication.tokenManager,
            MyApplication.activoApiService,
            this
        )
        registroControlRepository = RegistroControlRepository(
            MyApplication.registroControlApiService,
            context = this
        )
    }

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLeerTagSubsector.setOnClickListener {

            binding.btnIniciarControl.isEnabled = false
            binding.btnIniciarControl.iconTint = ColorStateList.valueOf(Color.GRAY)

            if (Configuracion.potenciaRFID != 5) {
                Log.d("CAMBIANDO POTENCIA RFID", "SE ESTA CAMBIANDO LA POTENCIA A 5")

                _oAxLector?.DetenetLecturRFID()

                _oAxLector?.LimpiarChainway()

                Configuracion.potenciaRFID = 5 ///VER

                _oAxLector?.IniciarLecturaRFID()

            }

            if (!leerTagSS) { //INICIA LECTURA
                binding.btnLeerTagSubsector.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.accent_color)
                )

                binding.btnLeerTagSubsector.text = "Leyendo..."

                leerTagSS = true
                Log.d("RFID ACTIVITY AGREGAR TAG A", "LECTURA INICIADA")
            }
            else {
                binding.btnLeerTagSubsector.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.black)
                )

                binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                listTagsLeidos.clear()

                leerTagSS = false
                Log.d("RFID ACTIVITY AGREGAR TAG A", "LECTURA PAUSADA")
            }
        }

        binding.btnIniciarControl.setOnClickListener {

            if (Configuracion.potenciaRFID != 30) {
                Log.d("CAMBIANDO POTENCIA RFID", "SE ESTA CAMBIANDO LA POTENCIA A 30")

                _oAxLector?.DetenetLecturRFID()

                _oAxLector?.LimpiarChainway()

                Configuracion.potenciaRFID = 30

                _oAxLector?.IniciarLecturaRFID()
            }

            isControlActive = !isControlActive

            if (isControlActive) {
                Toast.makeText(this, "Control Iniciado", Toast.LENGTH_SHORT).show()
                leerTagsControl = true

                binding.btnLeerTagSubsector.visibility = View.GONE
                binding.btnCancelarControl.visibility = View.VISIBLE

            }
            else {
                Toast.makeText(this, "Control Finalizado", Toast.LENGTH_SHORT).show()

                Log.d("PRUEBA IGUALDADES DE TAGS", "${activoList}")

                Log.d("PRUEBA IGUALDADES DE TAGS", "${listTagsLeidosControl}")

                binding.btnLeerTagSubsector.visibility = View.VISIBLE
                binding.btnCancelarControl.visibility = View.GONE

                if (activoListEncontrados.size > 1) {
                    crearControl()
                }
            }
            updateButtonUI()
        }

        binding.btnCancelarControl.setOnClickListener {
            Toast.makeText(this, "Control Cancelado", Toast.LENGTH_SHORT).show()

            binding.btnLeerTagSubsector.visibility = View.VISIBLE
            binding.btnCancelarControl.visibility = View.GONE

            isControlActive = false

            binding.btnIniciarControl.isEnabled = false


            prepararProximoControl()

            updateButtonUI()

            setupDataOnTable()
        }
    }

    private fun updateButtonUI() {
        binding.btnIniciarControl.apply {
            if (isControlActive) {
                text = "Finalizar"
                setIconResource(R.drawable.ic_stop)
            }
            else {
                text = "Iniciar"
                setIconResource(R.drawable.ic_play_arrow)
            }
        }
    }

    private fun setupNombreSubSector() {
        if (subSectorName != "") {
            binding.tvTableTitle.text = subSectorName
        }
        else {
            binding.tvTableTitle.text = "Control de Activos"
        }
    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        // ✅ Fuerza limpieza completa antes de cargar nuevos datos
        adapter.submitList(null) // Limpia completamente

        if (activoList.isNotEmpty()) {
            binding.btnIniciarControl.isEnabled = true
        } else {
            binding.btnIniciarControl.isEnabled = false
        }

        // ✅ Carga los nuevos datos en el siguiente frame
        Handler(Looper.getMainLooper()).post {
            adapter.submitList(activoList.toList()) // Crea nueva instancia de la lista
            progressBar.visibility = View.GONE
            emptyState.visibility = if (activoList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun obtenerTodosLosActivosEmpresa(idEmpresa: Int) {
        lifecycleScope.launch {
            val activos = activoRepository.obtenerTodosLosActivosEmpresa(idEmpresa)

            activos
                .onSuccess { lista ->
                    activosCompanyList = lista as MutableList<ObtenerActivosOSSDTO>
                }
                .onFailure { e ->
                    Log.d("ACTIVOS", "ERROR AL TRAER TODOS LOS ACTIVOS DE LA EMPRESA ${e}")

                    Toast.makeText(this@ControlOnlineActivity, "Error al cargar los activos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun obtenerActivos(tagRfid: String) {
        lifecycleScope.launch {
            val response = activoRepository.obtenerActivos(tagRfid,idCompany)
            response
                .onSuccess { lista ->

                    subSectorName = lista!!.subsector

                    setupNombreSubSector()

                    Log.d("ACTIVOS", "${lista}")

                    activoList = lista!!.activosDTO as MutableList<Activo>

                    idSubsectorControl = activoList[0].idSubsector

                    Log.d("ACTIVOS", "SECTORES TRAIDOS ${activoList}")

                    setupDataOnTable()

                    binding.btnIniciarControl.isEnabled = true
                    binding.btnIniciarControl.iconTint = ColorStateList.valueOf(Color.WHITE)
                }
                .onFailure { error ->

                    Log.d("ERROR AL OBTENER LOS ACTIVOS", "ERROR: ${response}")

                    Toast.makeText(this@ControlOnlineActivity, "Error al cargar los activos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun crearControl() {
        lifecycleScope.launch {

            val epcsLeidos = listTagsLeidosControl.map { it.TID }.toSet()

            activosCompanyList.forEach { activo ->
                if (activo.tagRfid in epcsLeidos && activo.idSubsector != activoList[0].idSubsector) {
                    Log.d("ACTIVO DE OTRO SECTOR ENCONTRADO", "${activo.name} ${activo.idSubsector} SECTOR CONTROLADO ${activoList[0].idSubsector}")

                    activoListEncontradosdeOtroSector.add(activo)
                }
            }

            if (activoListEncontradosdeOtroSector.size > 0) {

                lifecycleScope.launch {
                    val modal = ActivosEncontradosSSDialog(
                        this@ControlOnlineActivity,
                        activoListEncontradosdeOtroSector,
                        activoRepository
                    )
                    modal.setOnDismissListener {
                        activoListEncontradosdeOtroSector.clear()
                    }
                    modal.show()
                }

            }

            val timestamp = Instant.now().epochSecond

            val reponse = registroControlRepository.guardarRegistroControl(activoList[0].idSubsector, timestamp, idCompany) //////CAMBIARRR
            reponse
                .onSuccess { id ->
                    Log.d("REGISTRO CONTROL CREADO CON EXITO", "se creo CON EL ID ${id}")
                    Toast.makeText(this@ControlOnlineActivity, "Control guardado con exito", Toast.LENGTH_SHORT).show()

                    val idControl = id

                    crearDetallesControlEnCantidad(idControl!!)
                }
                .onFailure {
                    Log.d("REGISTRO CONTROL CREADO CON EXITO", "NO se creo ${reponse} ")
                    Toast.makeText(this@ControlOnlineActivity, "Error al crear el control", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun crearDetallesControlEnCantidad(idControl: Int) {
        lifecycleScope.launch {
            val detallesList = activoList.map { activo ->

                val status = if (activo.encontrado != "available") "notAvailable" else activo.encontrado!!

                CrearDetalleControlDTO(
                    idControl = idControl,
                    idActivo = activo.id,
                    status = status,
                    idAuditor = idUsuario
                )
            }

            val response = registroControlRepository.crearDetalleControlenCantidad(detallesList)

            if (response.isSuccess) {
                Log.d("DETALLE CONTROL GUARDADO CON EXITO", "${response.getOrNull()}")

                prepararProximoControl()
                setupDataOnTable()

            } else {
                Log.e("DETALLE CONTROL GUARDADO CON EXITO", "ERROR: ${response.exceptionOrNull()?.message}")

                prepararProximoControl()
                setupDataOnTable()
            }
        }
    }

    private fun prepararProximoControl() {
        leerTagsControl = false
        listTagsLeidosControl.clear()
        listTagsLeidos.clear()
        activoList.clear()
        idSubsectorControl = 0
        subSectorName = ""
        setupNombreSubSector()
    }
}