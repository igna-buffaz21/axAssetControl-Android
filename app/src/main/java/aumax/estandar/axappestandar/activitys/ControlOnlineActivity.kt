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
import aumax.estandar.axappestandar.databinding.ActivityControlOnlineBinding
import aumax.estandar.axappestandar.readers.AxLector
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.RegistroControlRepository
import aumax.estandar.axappestandar.utils.TagRFID
import aumax.estandar.axappestandar.utils.adapters.ActivoControlAdapter
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressDown
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressUp
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import java.time.Instant
import kotlinx.coroutines.launch

class ControlOnlineActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityControlOnlineBinding
    private lateinit var adapter: ActivoControlAdapter

    private lateinit var activoRepository: ActivoRepository
    private lateinit var registroControlRepository: RegistroControlRepository

    private var activoList: MutableList<Activo> = ArrayList()
    private var activoListEncontrados: MutableList<Activo> = ArrayList()
    private var idSubsectorControl: Int = 0

    private var isControlActive = false

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
                        Toast.makeText(this@ControlOnlineActivity, "Lectura no iniciada, presione Leer Tag Subsector", Toast.LENGTH_SHORT).show()
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
                            obtenerActivos(listTagsLeidos[0].EPC)
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

                        val epcsLeidos = listTagsLeidosControl.map { it.EPC }.toSet()

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
            MyApplication.registroControlApiService
        )
    }

    private fun setupListeners() {
        binding.btnLeerTagSubsector.setOnClickListener {

            if (Configuracion.potenciaRFID != 5) {

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

                _oAxLector?.DetenetLecturRFID()

                _oAxLector?.LimpiarChainway()

                Configuracion.potenciaRFID = 30 ///VER

                _oAxLector?.IniciarLecturaRFID()

            }

            isControlActive = !isControlActive

            if (isControlActive) {
                Toast.makeText(this, "Control Iniciado", Toast.LENGTH_SHORT).show()
                leerTagsControl = true
            }
            else {
                Toast.makeText(this, "Control Finalizado", Toast.LENGTH_SHORT).show()

                Log.d("PRUEBA IGUALDADES DE TAGS", "${activoList}")

                Log.d("PRUEBA IGUALDADES DE TAGS", "${listTagsLeidosControl}")

                if (activoListEncontrados.size > 1) {
                    crearControl()
                }
            }
            updateButtonUI()
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

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        if (activoList.isNotEmpty()) {
            binding.btnIniciarControl.isEnabled = true
        } else {
            binding.btnIniciarControl.isEnabled = false
        }

        adapter.submitList(activoList)
        progressBar.visibility = View.GONE
        emptyState.visibility = if (activoList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun obtenerActivos(tagRfid: String) {
        lifecycleScope.launch {
            val response = activoRepository.obtenerActivos(tagRfid)
            response
                .onSuccess { lista ->

                    activoList = lista as MutableList<Activo>

                    idSubsectorControl = activoList[0].idSubsector

                    Log.d("ACTIVOS", "SECTORES TRAIDOS ${activoList}")

                    setupDataOnTable()
                }
                .onFailure { error ->

                    Log.d("ERROR AL OBTENER LOS ACTIVOS", "ERROR: ${response}")

                    Toast.makeText(this@ControlOnlineActivity, "Error al cargar los activos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun crearControl() {
        lifecycleScope.launch {
            val timestamp = Instant.now().epochSecond

            val reponse = registroControlRepository.guardarRegistroControl(activoList[0].idSubsector, timestamp, 1)
            reponse
                .onSuccess { id ->
                    Log.d("REGISTRO CONTROL CREADO CON EXITO", "se creo CON EL ID ${id}")
                    Toast.makeText(this@ControlOnlineActivity, "Control guardado con exito", Toast.LENGTH_SHORT).show()

                    val idControl = id

                    crearDetallesControl(idControl!!)
                }
                .onFailure {
                    Log.d("REGISTRO CONTROL CREADO CON EXITO", "NO se creo ${reponse} ")
                    Toast.makeText(this@ControlOnlineActivity, "Error al crear el control", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun crearDetallesControl(idControl: Int) {
        lifecycleScope.launch {
            activoList.forEach { activo ->
                if (activo.encontrado != "available") {
                    activo.encontrado = "notAvailable"
                }

                val response = registroControlRepository.crearDetalleControl(idControl, activo.id, activo.encontrado!!, 1015)

                if (response.isSuccess) {
                    Log.d("DETALLE CONTROL GUARDADO CON EXITO", "${response} ")
                }
                else {
                    Log.d("DETALLE CONTROL GUARDADO CON EXITO", "ERROR: ${response} ")
                }
            }
        }
    }
}