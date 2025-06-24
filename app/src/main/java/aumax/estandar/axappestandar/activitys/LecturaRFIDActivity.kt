package aumax.estandar.axappestandar.activitys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import aumax.estandar.axappestandar.Adapters.LecturaRfidAdapter
import aumax.estandar.axappestandar.databinding.ActivityLecturaRfidBinding
import aumax.estandar.axappestandar.interfaces.IOnKeyPressDown
import aumax.estandar.axappestandar.interfaces.IOnKeyPressUp
import aumax.estandar.axappestandar.interfaces.ITagLeidoListener
import aumax.estandar.axappestandar.readers.AxLector
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.utils.TagRFID
import aumax.estandar.axappestandar.utils.Utils


class LecturaRFIDActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityLecturaRfidBinding
    private lateinit var _adapter: LecturaRfidAdapter
    private var toast: Toast? = null
    private var _oAxLector: AxLector? = null
    private var listenerKeyPressDown: IOnKeyPressDown? = null
    private var listenerKeyPressUp: IOnKeyPressUp? = null
    private var isReceiverRegistered = false

    private var listTagsLeidos : MutableList<TagRFID> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLecturaRfidBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(_binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        _oAxLector = AxLector(this@LecturaRFIDActivity, Configuracion.MODO_LECTURA.RFID)

        _adapter = LecturaRfidAdapter()
        _binding.listReading.adapter = _adapter
        _binding.listReading.layoutManager = LinearLayoutManager(this)


        setListenerKeyPressDown(object : IOnKeyPressDown {
            override fun keyPress(keyCode: Int, event: KeyEvent?) {
                if (event!!.repeatCount == 0) {
                    _oAxLector?.IniciarLecturaRFID()
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

        RegistrarEventLecturaTag()
    }

    override fun onStart() {
        super.onStart()

        if (!isReceiverRegistered ){
            val filter = IntentFilter()
            filter.addAction("android.rfid.FUN_KEY")
            registerReceiver(receiver, filter)

            if(Configuracion.tipoHandheld == Configuracion.TIPO_HANDHELD.Linkwin &&
                _oAxLector?._modoLectura == Configuracion.MODO_LECTURA.Codigo ||
                _oAxLector?._modoLectura == Configuracion.MODO_LECTURA.Ambas) {
                val filter = IntentFilter()
                filter.addAction("com.rfid.SCAN")
                registerReceiver(receiverCB, filter)
                isReceiverRegistered = true // Cambiar la bandera a true
            }
        }
    }


    override fun onStop() {
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
        }

    }


    private fun setListenerKeyPressDown(listener: IOnKeyPressDown) {
        this.listenerKeyPressDown = listener
    }
    private fun setListenerKeyPressUp(listener: IOnKeyPressUp) {
        this.listenerKeyPressUp = listener
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        //Log.d("KEY_EVENT", "keyDown: $keyCode")
        if (keyCode == 139 || keyCode == 280 || keyCode == 293) {
            if (this.listenerKeyPressDown != null) {
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
        _oAxLector!!.setListenerTagLeido(object : ITagLeidoListener {
            override fun tagsLeidos(listTagsLeidos: MutableList<TagRFID>) {

            }

            override fun tagLeido(tagRFID: TagRFID) {
                val tagExistente = listTagsLeidos.find { it.TID == tagRFID.TID }

                if (tagExistente != null) {
                    tagExistente.Count += 1
                } else {
                    listTagsLeidos.add(tagRFID)
                }

                runOnUiThread {
                    val nuevaLista =listTagsLeidos.map { it.copy() }
                    _adapter.submitList(nuevaLista)
                }
            }

            override fun error(mensaje: String) {
                runOnUiThread {
                    toast = Utils.ToastUtils.toastError(this@LecturaRFIDActivity, "$mensaje")
                    toast?.show()
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


}