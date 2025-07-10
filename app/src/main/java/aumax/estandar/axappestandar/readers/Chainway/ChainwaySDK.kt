package aumax.estandar.axappestandar.readers.Chainway

import android.util.Log
import aumax.estandar.axappestandar.interfaces.ITagLeidoListener
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.utils.TagRFID
import com.rscja.deviceapi.RFIDWithUHFUART
import com.rscja.deviceapi.entity.UHFTAGInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ChainwaySDK {

    var mReader: RFIDWithUHFUART? = null;
    var lectorIniciado = false;
    var leyendo = false;
    var listenerITagLeido: ITagLeidoListener? = null

    fun Iniciar() {
        mReader = RFIDWithUHFUART.getInstance()
        mReader = RFIDWithUHFUART.getInstance()

        if (!lectorIniciado) {
            synchronized(this) {
                mReader?.init()
                mReader?.power = Configuracion.potenciaRFID
                Log.i("CHAINWAY SDK", "${mReader?.power}")
                lectorIniciado = true
            }
        }
    }

    fun LeerTags(){
        try {
            mReader!!.setEPCAndTIDMode()
            if (mReader!!.startInventoryTag()) {
                CoroutineScope(Dispatchers.IO).launch {
                    LecturaDeTags()
                }
            } else {
                mReader!!.stopInventory()
                lectorIniciado = false
            }
        } catch (ex: Exception) {
            listenerITagLeido?.error("ERROR DE LECTURA")
        }
    }



    fun LecturaDeTags() {
        try{
            var res: UHFTAGInfo? = null

            //genero evento indicando que estoy leyendo
            listenerITagLeido!!.estado(Configuracion.ESTADOS_LECTURA.INICIADO)

            var listTagsLeidos : MutableList<TagRFID> = ArrayList<TagRFID>()

            leyendo = true
            while (leyendo) {
                res = mReader!!.readTagFromBuffer()
                if (res != null) {

                    var tagRFID = TagRFID(
                        TID = res.tid,
                        EPC = res.epc,
                        RSSI = res.rssi,
                        Count = 1
                    )

                    val tagExistente = listTagsLeidos.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1
                    } else {
                        listTagsLeidos.add(tagRFID)
                    }

                    listenerITagLeido!!.tagLeido(tagRFID)
                    Log.e("TAG LEIDO",res.tid)
                }
            }

            mReader!!.stopInventory()
            if(listTagsLeidos.isNotEmpty()){
                listenerITagLeido!!.tagsLeidos(listTagsLeidos)
            }else{
                listenerITagLeido!!.error("No se pudo leer ningún tag RFID.")
            }

        }catch (ex: Exception){
            leyendo = false
            Log.e("Error en run","$ex")
            listenerITagLeido!!.estado(Configuracion.ESTADOS_LECTURA.ERROR)
            listenerITagLeido!!.error("Error de lectura " + ex.message.toString())
        }

    }

    fun dispose() {
        if (mReader != null) {
            mReader!!.free()
            lectorIniciado = false
            mReader = null
        }
    }

    fun setCustomObjectListener(listener: ITagLeidoListener?) {
        listenerITagLeido = listener
    }
}