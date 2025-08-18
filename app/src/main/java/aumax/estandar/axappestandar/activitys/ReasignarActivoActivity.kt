package aumax.estandar.axappestandar.activitys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import aumax.estandar.axappestandar.MyApplication
import aumax.estandar.axappestandar.R
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.SubSector.SubSector
import aumax.estandar.axappestandar.databinding.ActivityReasignarActivoBinding
import aumax.estandar.axappestandar.readers.AxLector
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.repository.ActivoRepository
import aumax.estandar.axappestandar.repository.LocacionRepository
import aumax.estandar.axappestandar.repository.SectorRepository
import aumax.estandar.axappestandar.repository.SubSectorRepository
import aumax.estandar.axappestandar.utils.TagRFID
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressDown
import aumax.estandar.axappestandar.utils.interfaces.IOnKeyPressUp
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import kotlinx.coroutines.launch

class ReasignarActivoActivity(
) : AppCompatActivity() {

    private lateinit var binding: ActivityReasignarActivoBinding

    private lateinit var subsectorRepository: SubSectorRepository
    private lateinit var activoRepository: ActivoRepository

    private var ActivoAReasginar: Activo? = null
    private var subSectorAsignar: SubSector? = null


    //RFID
    private var _oAxLector: AxLector? = null
    private var listenerKeyPressDown: IOnKeyPressDown? = null
    private var listenerKeyPressUp: IOnKeyPressUp? = null
    private var isReceiverRegistered = false
    private var listTagsLeidosA : MutableList<TagRFID> = ArrayList()
    private var listTagsLeidosSS : MutableList<TagRFID> = ArrayList()

    //FLAG
    private var leerTag: Boolean = false
    private var leerTagSS: Boolean = false
    private var confirmar: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReasignarActivoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _oAxLector = AxLector(this@ReasignarActivoActivity, Configuracion.MODO_LECTURA.RFID) //creamos el objeto que va a manejar todo el RFID

        setListenerKeyPressDown(object : IOnKeyPressDown { //CONFIGURO qué hacer cuando se presione la tecla
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) { //evitar repeticiones por mantener presionado
                    if (leerTag && !leerTagSS) {
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                    }
                    else if (!leerTag && leerTagSS) {
                        _oAxLector?.IniciarLecturaRFID() //acción a ejecutar
                    }
                    else {
                        Toast.makeText(this@ReasignarActivoActivity, "Inicie la lectura", Toast.LENGTH_LONG).show()
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
        setupListeners()
        RegistrarEventLecturaTag()
    }

    private fun setupRepositorys() {
        subsectorRepository = SubSectorRepository(
            MyApplication.tokenManager,
            MyApplication.subSectorApiService,
            context = this
        )

        activoRepository = ActivoRepository(
            MyApplication.tokenManager,
            MyApplication.activoApiService,
            context = this
        )
    }

    private fun setupHeaderComponent() {
        val tokenManager = MyApplication.tokenManager

        val username = tokenManager.obtenerNombreUsuario()
        val nombreEmpresa = tokenManager.obtenerNombreEmpresa()
        binding.header.tvCompanyName.text = nombreEmpresa
        binding.header.tvUserName.text = username
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
                        resetBTNA()

                        runOnUiThread {
                            Toast.makeText(this@ReasignarActivoActivity, "Se detectaron múltiples tags, intenta nuevamente.", Toast.LENGTH_LONG).show()
                        }

                        Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 1")

                        return
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (listTagsLeidosA.size == 1 && leerTag) {
                            obtenerActivoPorRfid(tagRFID, 1)

                            leerTag = false
                            listTagsLeidosA.clear()
                            resetBTNA()
                        }
                        else {
                            leerTag = false
                            listTagsLeidosA.clear()
                            Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 2")
                            resetBTNA()

                        }
                    }, 500)

                }

                else if (leerTagSS) {

                    val tagExistente = listTagsLeidosSS.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1 //si ya se leyo ese tag no agrega
                    } else {
                        listTagsLeidosSS.add(tagRFID) //si no se leyo se agrega
                    }

                    // Si hay más de un tag leído, cancelar operación
                    if (listTagsLeidosSS.size > 1) {
                        leerTagSS = false
                        listTagsLeidosSS.clear()
                        resetBTNSS()

                        Log.d("PRUEBA LECTOR", "Se canceló porque se detectaron múltiples tags desde 1 ${listTagsLeidosSS}")

                        runOnUiThread {
                            Toast.makeText(this@ReasignarActivoActivity, "Se detectaron múltiples tags, intenta nuevamente 1.", Toast.LENGTH_LONG).show()
                        }

                        //activarBTNSS()

                        return
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (listTagsLeidosSS.size == 1 && leerTagSS) {
                            leerTagSS = false // bloquea antes de limpiar o llamar
                            obtenerSubSectorPorRfid(tagRFID, 1)
                            listTagsLeidosSS.clear()
                            resetBTNSS()
                        } else {
                            leerTagSS = false
                            listTagsLeidosSS.clear()
                            resetBTNSS()
                            //Toast.makeText(this@ReasignarActivoActivity, "Se detectaron múltiples tags, intenta nuevamente 2.", Toast.LENGTH_LONG).show()
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

    private fun setupListeners() {
        binding.header.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLeerTagActivo.setOnClickListener {

            if (!leerTag) {
                if (Configuracion.potenciaRFID != 5) {
                    Log.d("CAMBIANDO POTENCIA RFID", "SE ESTA CAMBIANDO LA POTENCIA A 5")

                    _oAxLector?.DetenetLecturRFID()

                    _oAxLector?.LimpiarChainway()

                    Configuracion.potenciaRFID = 5 ///VER

                    _oAxLector?.IniciarLecturaRFID()

                }

                binding.btnLeerTagActivo.text = "Leyendo..."

                binding.btnLeerTagActivo.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.accent_color)
                )

                leerTag = true
            }
            else {
                leerTag = false

                binding.btnLeerTagActivo.text = "Leer Tag Activo"

                binding.btnLeerTagActivo.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.black)
                )

            }
        }

        binding.btnLeerTagSubsector.setOnClickListener {
            if (!leerTagSS) {
                if (Configuracion.potenciaRFID != 5) {
                    Log.d("CAMBIANDO POTENCIA RFID", "SE ESTA CAMBIANDO LA POTENCIA A 5")

                    _oAxLector?.DetenetLecturRFID()

                    _oAxLector?.LimpiarChainway()

                    Configuracion.potenciaRFID = 5 ///VER

                    _oAxLector?.IniciarLecturaRFID()

                }

                binding.btnLeerTagSubsector.text = "Leyendo..."

                binding.btnLeerTagSubsector.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.accent_color)
                )

                leerTagSS = true

            }
            else {
                leerTag = false

                binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                binding.btnLeerTagSubsector.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.black)
                )
            }
        }

        binding.btnConfirmar.setOnClickListener {
            if (confirmar) {
                ReasignardeLugar()
            }
        }
    }

    private fun obtenerActivoPorRfid(tagRfid: TagRFID, idEmpresa: Int) {
        lifecycleScope.launch {
            val response = activoRepository.obtenerActivosPorRfid(tagRfid.TID, idEmpresa)

            response
                .onSuccess { activo ->
                    Toast.makeText(this@ReasignarActivoActivity, "Activo a reasignar ${activo!!.name}", Toast.LENGTH_SHORT).show()

                    binding.btnLeerTagActivo.text = "Leer Tag Activo"

                    binding.btnLeerTagActivo.setBackgroundColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.black)
                    )

                    binding.btnLeerTagSubsector.isEnabled = true

                    binding.btnLeerTagSubsector.setBackgroundColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.black)
                    )

                    binding.btnLeerTagSubsector.setTextColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.white)
                    )

                    ActivoAReasginar = activo
                }

                .onFailure { error ->
                    Toast.makeText(this@ReasignarActivoActivity, "No se encontro ningun activo", Toast.LENGTH_SHORT).show()

                    binding.btnLeerTagActivo.text = "Leer Tag Activo"

                    binding.btnLeerTagActivo.setBackgroundColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.black)
                    )

                    binding.btnLeerTagSubsector.isEnabled = false

                    binding.btnLeerTagSubsector.setBackgroundColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.enabledfalse)
                    )

                    binding.btnLeerTagSubsector.setTextColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.mediumGray)
                    )

                }
        }
        }

    private fun obtenerSubSectorPorRfid(tagRfid: TagRFID, idEmpresa: Int) {
        lifecycleScope.launch {
            val response = subsectorRepository.obtenerSectoresPorRfid(tagRfid.TID, idEmpresa)

            response
                .onSuccess { subSector ->
                    Toast.makeText(this@ReasignarActivoActivity, "Subsector ${subSector!!.name}", Toast.LENGTH_SHORT).show()

                    binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

                    binding.btnLeerTagSubsector.setBackgroundColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.black)
                    )

                    subSectorAsignar = subSector

                    binding.btnConfirmar.isEnabled = true

                    binding.btnConfirmar.setBackgroundColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.black)
                    )

                    binding.btnConfirmar.setTextColor(
                        ContextCompat.getColor(this@ReasignarActivoActivity, R.color.white)
                    )

                    confirmar = true
                }

                .onFailure { error ->
                    Toast.makeText(this@ReasignarActivoActivity, "No se encontro ningun subsector", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun ReasignardeLugar() {
        lifecycleScope.launch {
            if (ActivoAReasginar != null && subSectorAsignar != null) {
                val response = activoRepository.reasignarActivo(ActivoAReasginar!!.id, subSectorAsignar!!.id)

                response
                    .onSuccess {
                        Toast.makeText(this@ReasignarActivoActivity, "Se asigno: ${ActivoAReasginar!!.name} a ${subSectorAsignar!!.name}", Toast.LENGTH_SHORT).show()

                        limpiarVariables()
                        desactivarBTNSS()
                        desactivarBTNC()
                    }
                    .onFailure { error ->
                        Toast.makeText(this@ReasignarActivoActivity, "Error al reasignar el activo, intentelo de nuevo!", Toast.LENGTH_SHORT).show()
                        Log.d("ERROR AL REASIGNAR ACTIVO", "${response})")
                        Log.d("ERROR AL REASIGNAR ACTIVO", "${error})")

                        limpiarVariables()
                        desactivarBTNSS()
                        desactivarBTNC()
                    }
            }
        }
    }

    private fun desactivarBTNSS() {
        binding.btnLeerTagSubsector.isEnabled = false

        binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

        binding.btnLeerTagSubsector.setBackgroundColor(
            ContextCompat.getColor(this, R.color.enabledfalse)
        )

        binding.btnLeerTagSubsector.setTextColor(
            ContextCompat.getColor(this, R.color.mediumGray)
        )

        leerTagSS = false
    }

    private fun desactivarBTNC() {
        binding.btnConfirmar.isEnabled = false

        binding.btnConfirmar.setBackgroundColor(
            ContextCompat.getColor(this, R.color.enabledfalse)
        )

        binding.btnConfirmar.setTextColor(
            ContextCompat.getColor(this, R.color.mediumGray)
        )

        confirmar = false
    }

    private fun resetBTNA() {
        binding.btnLeerTagActivo.isEnabled = true

        binding.btnLeerTagActivo.setBackgroundColor(
            ContextCompat.getColor(this, R.color.black)
        )

        binding.btnLeerTagActivo.text = "Leer Tag Activo"

        binding.btnLeerTagActivo.setTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
    }

    private fun resetBTNSS() {
        binding.btnLeerTagSubsector.isEnabled = true

        binding.btnLeerTagSubsector.setBackgroundColor(
            ContextCompat.getColor(this, R.color.black)
        )

        binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

        binding.btnLeerTagSubsector.setTextColor(
            ContextCompat.getColor(this, R.color.white)
        )
    }

    private fun limpiarVariables() {
        listTagsLeidosA.clear()
        listTagsLeidosSS.clear()
        ActivoAReasginar = null
        subSectorAsignar = null
    }

    private fun activarBTNSS() {
        binding.btnLeerTagSubsector.isEnabled = true

        binding.btnLeerTagSubsector.text = "Leer Tag Subsector"

        binding.btnLeerTagSubsector.setBackgroundColor(
            ContextCompat.getColor(this, R.color.black)
        )

        binding.btnLeerTagSubsector.setTextColor(
            ContextCompat.getColor(this, R.color.white)
        )

        leerTagSS = true
    }

}