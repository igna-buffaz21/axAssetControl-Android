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
import aumax.estandar.axappestandar.databinding.ActivityAgregarTagABinding
import aumax.estandar.axappestandar.readers.AxLector
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.utils.TagRFID
import aumax.estandar.axappestandar.utils.adapters.ActivoAdapter
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressDown
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressUp
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import kotlinx.coroutines.launch

class ReasignarTagActivoActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarTagABinding

    private lateinit var adapter: ActivoAdapter

    private lateinit var activoRepository: ActivoRepository
    private var activoList: List<Activo> = emptyList()

    private var activoListMostrar: MutableList<Activo> = ArrayList()

    //RFID
    private var _oAxLector: AxLector? = null
    private var listenerKeyPressDown: IOnKeyPressDown? = null
    private var listenerKeyPressUp: IOnKeyPressUp? = null
    private var isReceiverRegistered = false
    private var listTagsLeidos : MutableList<TagRFID> = ArrayList()
    private var listTagsLeidosA : MutableList<TagRFID> = ArrayList()

    //FLAG
    private var leerTagSS: Boolean = false
    private var leerTagA: Boolean = false

    private lateinit var activoAAsignar: Activo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarTagABinding.inflate(layoutInflater)
        setContentView(binding.root)

        _oAxLector = AxLector(this@ReasignarTagActivoActivity, Configuracion.MODO_LECTURA.RFID) //creamos el objeto que va a manejar todo el RFID

        setListenerKeyPressDown(object : IOnKeyPressDown { //CONFIGURO qué hacer cuando se presione la tecla
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) { //evitar repeticiones por mantener presionado
                    if (leerTagSS && !leerTagA) {
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                    }
                    else if (!leerTagSS && leerTagA) {
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                    }
                    else {
                        Toast.makeText(this@ReasignarTagActivoActivity, "Lectura no iniciada, presione Leer Tag Subsector", Toast.LENGTH_SHORT).show()
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

        setupHeaderComponent()
        setupTableComponent()
        setupRepository()
        RegistrarEventLecturaTag()
        setupListeners()
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

    private fun setupRepository() {
        activoRepository = ActivoRepository(
            MyApplication.tokenManager,
            MyApplication.activoApiService,
            this
        )
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
                            ContextCompat.getColor(this@ReasignarTagActivoActivity, R.color.black)
                        )

                        binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                        runOnUiThread {
                            Toast.makeText(this@ReasignarTagActivoActivity, "Se detectaron múltiples tags, intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }

                        Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 1")

                        return
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (listTagsLeidos.size == 1 && leerTagSS) {
                            obtenerActivos(listTagsLeidos[0])
                            listTagsLeidos.clear()
                            leerTagSS = false

                            binding.btnLeerTagSubsector.setBackgroundColor(
                                ContextCompat.getColor(this@ReasignarTagActivoActivity, R.color.black)
                            )

                            binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                        }
                        else {
                            Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 2")
                            listTagsLeidos.clear()
                            leerTagSS = false

                            binding.btnLeerTagSubsector.setBackgroundColor(
                                ContextCompat.getColor(this@ReasignarTagActivoActivity, R.color.black)
                            )

                            binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                        }
                    }, 500) // Espera 500ms para ver si se suma otro tag


                }

                if (leerTagA) {
                    Log.d("PRUEBA LECTOR", "entro a tagA")

                    val tagExistente = listTagsLeidosA.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1 //si ya se leyo ese tag no agrega
                    }
                    else {
                        //listTagsLeidosA.clear()
                        listTagsLeidosA.add(tagRFID) //si no se leyo se agrega
                        Log.d("PRUEBA LECTOR", " TAG LEIDOS PARA LEER TAG A ${listTagsLeidosA}")
                    }

                    // Si hay más de un tag leído, cancelar operación
                    if (listTagsLeidosA.size > 1) {
                        leerTagA = false
                        listTagsLeidosA.clear()
                        adapter.deactivateActiveItem()

                        runOnUiThread {
                            Toast.makeText(this@ReasignarTagActivoActivity, "Se detectaron múltiples tags, intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }

                        Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags")

                        return
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (listTagsLeidosA.size == 1 && leerTagA) {
                            guardarTag(activoAAsignar, listTagsLeidosA[0])

                            leerTagA = false
                            listTagsLeidosA.clear()
                            adapter.deactivateActiveItem()
                        }
                        else {
                            Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 2")
                            leerTagA = false
                            listTagsLeidosA.clear()
                            adapter.deactivateActiveItem()
                        }
                    }, 500) // Espera 500ms para ver si se suma otro tag
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

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLeerTagSubsector.setOnClickListener {

            if (!leerTagSS) { //INICIA LECTURA

                if (Configuracion.potenciaRFID != 5) {

                    _oAxLector?.DetenetLecturRFID()

                    _oAxLector?.LimpiarChainway()

                    Configuracion.potenciaRFID = 5 ///VER

                    _oAxLector?.IniciarLecturaRFID()

                }

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

        adapter.onAddClick = { activo ->

            if (Configuracion.potenciaRFID != 5) {
                Log.d("CAMBIANDO POTENCIA RFID", "SE ESTA CAMBIANDO LA POTENCIA A 5")

                _oAxLector?.DetenetLecturRFID()

                _oAxLector?.LimpiarChainway()

                Configuracion.potenciaRFID = 5 ///VER

                _oAxLector?.IniciarLecturaRFID()

            }

            leerTagA = true

            activoAAsignar = activo

            //Toast.makeText(this@AgregarTagAActivity, "Leyendo tag para ${activo.name}", Toast.LENGTH_SHORT).show()

        }
    }

    private fun setupHeaderComponent() {
        val tokenManager = MyApplication.tokenManager

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()
        binding.header.tvCompanyName.text = nombreEmpresa
        binding.header.tvUserName.text = username
    }

    private fun setupDataOnTable() {
        val progressBar = binding.tableComponent.progressBar
        val emptyState = binding.tableComponent.emptyState

        progressBar.visibility = View.VISIBLE

        adapter.submitList(null) // Limpia completamente

        // ✅ Carga los nuevos datos en el siguiente frame
        Handler(Looper.getMainLooper()).post {
            adapter.submitList(activoListMostrar.toList()) // Crea nueva instancia de la lista
            progressBar.visibility = View.GONE
            emptyState.visibility = if (activoListMostrar.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun obtenerActivos(tagRfid: TagRFID) {
        lifecycleScope.launch {
            //Toast.makeText(this@AgregarTagAActivity, "Tag Recibido ${tagRfid.EPC}", Toast.LENGTH_SHORT).show()
            val response = activoRepository.obtenerActivos(tagRfid.TID.toString())
            response
                .onSuccess { lista ->
                    setupDataOnTable()

                    activoList = lista ?: emptyList()
                    activoListMostrar.clear()

                    activoList.forEach { activo ->
                        if (activo.tagRfid != null) {
                            activoListMostrar.add(activo)
                        }
                    }

                    if (activoListMostrar.size == 0) {
                        Toast.makeText(this@ReasignarTagActivoActivity, "No hay ningun activo para reasignar tag", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("ACTIVOS", "SECTORES TRAIDOS ${activoListMostrar}")

                    setupDataOnTable()

                }
                .onFailure { error ->

                    activoList = emptyList()
                    activoListMostrar.clear()

                    Log.d("ERROR AL OBTENER LOS ACTIVOS", "ERROR: ${response}")

                    setupDataOnTable()

                    Toast.makeText(this@ReasignarTagActivoActivity, "Error al cargar los activos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarTag(activo: Activo, tagCode: TagRFID) {
        lifecycleScope.launch {
            val response = activoRepository.asigarTagRfidActivo(tagCode.TID, activo.id, 1)

            response
                .onSuccess {
                    Toast.makeText(this@ReasignarTagActivoActivity, "Tag asignado con exito", Toast.LENGTH_SHORT).show()
                    listTagsLeidosA.clear()
                    leerTagA = false

                    val positionA = activoListMostrar.indexOfFirst { it.id == activo.id } //buscamos indice del actualizado

                    if (positionA != -1) {
                        val updatedActivo = activoListMostrar[positionA].copy(tagRfid = tagCode.TID) //copia nueva del subsector actualizado

                        val nuevaLista = activoListMostrar.toMutableList()
                        nuevaLista[positionA] = updatedActivo

                        activoListMostrar = nuevaLista

                        adapter.submitList(nuevaLista)
                    }

                }
                .onFailure {
                    Toast.makeText(this@ReasignarTagActivoActivity, "Error: ${response}", Toast.LENGTH_SHORT).show()
                    Log.d("PRUEBA LECTOR", "ERROR AL ASIGNAR TAG ${response}")
                    listTagsLeidosA.clear()
                    leerTagA = false
                }
        }
    }
}