package aumax.estandar.axappestandar.readers

import android.content.Context
import android.util.Log
import aumax.estandar.axappestandar.utils.interfaces.ICodigoLeido
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import aumax.estandar.axappestandar.readers.Chainway.ChainwaySDK
import aumax.estandar.axappestandar.readers.Linkwin.LinkWinSDK
import aumax.estandar.axappestandar.readers.Linkwin.LinkWinScanUtils
import aumax.estandar.axappestandar.utils.TagRFID
import com.rscja.barcode.BarcodeDecoder
import com.rscja.barcode.BarcodeFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AxLector(private val context: Context, private val modoLectura : Int) {

    lateinit var chainwaySDK: ChainwaySDK
    lateinit var linkWinSDK: LinkWinSDK
    var scanUtil: LinkWinScanUtils? = null
    private var job: Job? = null
    var barcodeDecoder = BarcodeFactory.getInstance().barcodeDecoder

    var listenerITagLeido: ITagLeidoListener? = null
    var listenerICodigoLeido: ICodigoLeido? = null
    var _modoLectura = 0

    init { //se ejecuta cuando se crea el objeto
        try {
            _modoLectura = modoLectura
            when(Configuracion.tipoHandheld){
                Configuracion.TIPO_HANDHELD.Linkwin->{
                    IniciarLinkWin() //probar
                }
                Configuracion.TIPO_HANDHELD.ChainwayC72->{
                    IniciarChainWay()
                }
            }
        } catch (e: Exception) {
            Log.e("Error al inicializar lector", e.message.toString())
        }

    }

    //HANDHELD CHAINWAY
    private fun IniciarChainWay() {
        //INIICIAR CB
        //C72
        when(_modoLectura) {
            Configuracion.MODO_LECTURA.RFID -> {
                IniciarRFIDChainway()
            }
            Configuracion.MODO_LECTURA.Codigo -> {
                job = CoroutineScope(Dispatchers.Main).launch {
                    IniciarCodigoChainway()
                }
            }
            Configuracion.MODO_LECTURA.Ambas -> {
                job = CoroutineScope(Dispatchers.Main).launch {
                    IniciarCodigoChainway()
                }
                IniciarRFIDChainway()
            }
        }
    }

    private fun IniciarRFIDChainway(){
        chainwaySDK = ChainwaySDK
        chainwaySDK.Iniciar()
        if (chainwaySDK.lectorIniciado) {
            chainwaySDK.setCustomObjectListener(object : ITagLeidoListener {
                override fun tagsLeidos(listTagsLeidos: MutableList<TagRFID>) {
                    listenerITagLeido!!.tagsLeidos(listTagsLeidos)
                }

                override fun tagLeido(tagRFID: TagRFID) {
                    listenerITagLeido!!.tagLeido(tagRFID)
                }
                override fun error(mensaje: String) {
                    listenerITagLeido!!.error(mensaje)
                }

                override fun estado(estado: Int) {
                    listenerITagLeido!!.estado(estado)
                }

            })
        }
    }

    private fun IniciarCodigoChainway() {
        barcodeDecoder.open(context)
        barcodeDecoder.setParameter(0, 1)
        barcodeDecoder.setDecodeCallback { barcodeEntity ->
            if (barcodeEntity.resultCode == BarcodeDecoder.DECODE_SUCCESS) {
                Log.e("CB= ", barcodeEntity.barcodeData)
                listenerICodigoLeido!!.codigoLeido(barcodeEntity.barcodeData)
            } else {
                listenerICodigoLeido!!.error("NO SE LEYO NINGUN CODIGO")
            }
        }
    }

    private fun LeerCodigoChainway(){
        try{
            if (barcodeDecoder.isOpen) {
                barcodeDecoder.setTimeOut(10)
                barcodeDecoder.startScan()
            }else{
                if(job == null){
                    job = CoroutineScope(Dispatchers.Main).launch {
                        IniciarCodigoChainway()
                    }
                }
            }
        }catch (e: Exception) {
            Log.e("Error al leerCodigoChainway", e.message.toString())
        }

    }

    private fun LeerTagsChainway() {
        if(ChainwaySDK.lectorIniciado){
            ChainwaySDK.LeerTags()
        }else{
            IniciarChainWay()
        }
    }

    //HANDHELD LINKWIN
    private fun IniciarLinkWin() {
        when(_modoLectura) {
            Configuracion.MODO_LECTURA.RFID -> {
                IniciarRFIDLinkWin()
            }
            Configuracion.MODO_LECTURA.Codigo -> {
                //no se inicializa
            }
            Configuracion.MODO_LECTURA.Ambas -> {
                IniciarRFIDLinkWin()
            }
        }
    }

    private fun IniciarRFIDLinkWin(){
        linkWinSDK = LinkWinSDK
        linkWinSDK.iniciar(Configuracion.potenciaRFID)
        if (linkWinSDK.lectorIniciado) {
            linkWinSDK.setCustomObjectListener(object : ITagLeidoListener {
                override fun tagsLeidos(listTagsLeidos: MutableList<TagRFID>) {
                    listenerITagLeido!!.tagsLeidos(listTagsLeidos)
                }

                override fun tagLeido(tagRFID: TagRFID) {
                    listenerITagLeido!!.tagLeido(tagRFID)
                }

                override fun error(mensaje: String) {
                    listenerITagLeido!!.error(mensaje)
                }

                override fun estado(estado: Int) {
                    listenerITagLeido!!.estado(estado)
                }
            })
        }
    }

    fun LeerCodigoLinkwin() {
        try {
            Log.e("CB= ","INICIA LECTURA CB")
            scanUtil = LinkWinScanUtils(context)
            scanUtil?.setScanMode(0)
            scanUtil?.scan()
            // scanUtil?.setScanMode(0)
        } catch (ex: Exception) {
            listenerICodigoLeido?.error("Error al iniciar lectura: $ex")
        }
    }

    fun LeerTagsLinkWin() {
        LinkWinSDK.LeerTags()
    }
    //LISTENER
    fun setListenerTagLeido(listener: ITagLeidoListener?) {
        listenerITagLeido = listener
    }

    fun setListenerCodigoLedido(listener: ICodigoLeido?) {
        listenerICodigoLeido = listener
    }

    fun IniciarLecturaRFID(){
        when(Configuracion.tipoHandheld){
            Configuracion.TIPO_HANDHELD.Linkwin->{
                //ver el manejo de inicio parada
                LeerTagsLinkWin()
            }
            Configuracion.TIPO_HANDHELD.ChainwayC72->{
                if(!chainwaySDK.leyendo){
                    LeerTagsChainway()
                }
            }
        }
    }

    fun DetenetLecturRFID(){
        when(Configuracion.tipoHandheld){
            Configuracion.TIPO_HANDHELD.Linkwin->{
                //ver el manejo de parada
            }
            Configuracion.TIPO_HANDHELD.ChainwayC72->{
                chainwaySDK.leyendo = false
            }
        }
    }

    fun LeerCodigo(){
        when(Configuracion.tipoHandheld){
            Configuracion.TIPO_HANDHELD.Linkwin->{
                LeerCodigoLinkwin()
            }
            Configuracion.TIPO_HANDHELD.ChainwayC72->{
                LeerCodigoChainway()
            }
        }
    }


    //FINALIZAR LECTURA
    fun FinalizarLectura(){
        try{
            when(Configuracion.tipoHandheld){
                Configuracion.TIPO_HANDHELD.Linkwin->{
                    when(_modoLectura) {
                        Configuracion.MODO_LECTURA.RFID -> {
                            linkWinSDK.CerrarLectura()
                            linkWinSDK.lectorIniciado = false
                        }
                        Configuracion.MODO_LECTURA.Codigo -> {
                            scanUtil?.stopScan()
                            scanUtil?.close()
                        }
                        Configuracion.MODO_LECTURA.Ambas -> {
                            scanUtil?.stopScan()
                            scanUtil?.close()
                            linkWinSDK.CerrarLectura()
                            linkWinSDK.lectorIniciado = false
                        }
                    }
                }
                Configuracion.TIPO_HANDHELD.ChainwayC72->{
                    when(_modoLectura) {
                        Configuracion.MODO_LECTURA.RFID -> {
                            chainwaySDK.dispose()
                            chainwaySDK.lectorIniciado = false
                        }
                        Configuracion.MODO_LECTURA.Codigo -> {
                            barcodeDecoder.close()
                            job?.cancel()
                        }
                        Configuracion.MODO_LECTURA.Ambas -> {
                            barcodeDecoder.close()
                            job?.cancel()
                            chainwaySDK.dispose()
                            chainwaySDK.lectorIniciado = false
                        }
                    }
                }
            }
        }catch (ex : Exception){
            Log.e("finalizarLectura0", "${ex.message}")
        }
    }
}