package aumax.estandar.axappestandar.readers.Linkwin

import android.app.Application
import android.util.Log
import aumax.estandar.axappestandar.utils.interfaces.ITagLeidoListener
import aumax.estandar.axappestandar.readers.Chainway.ChainwaySDK
import aumax.estandar.axappestandar.readers.Configuracion
import aumax.estandar.axappestandar.utils.TagRFID
import cn.pda.serialport.Tools
import com.handheld.uhfr.UHFRManager
import com.uhf.api.cls.Reader
import com.uhf.api.cls.Reader.READER_ERR

object LinkWinSDK : Application() {

    var mUhfrManager: UHFRManager? = null
    var lectorIniciado = false
    var listenerITagLeido: ITagLeidoListener? = null


    fun iniciar(potRead: Int) {
        try {

            mUhfrManager = UHFRManager.getInstance()
            val err: READER_ERR = mUhfrManager!!.setPower(potRead, 30) //set uhf module power
            mUhfrManager!!.region = Reader.Region_Conf.valueOf(Reader.Region_Conf.RG_NA.value())
            lectorIniciado = true
        } catch (ex: Exception) {

        }


    }

    fun setCustomObjectListener(listener: ITagLeidoListener?) {
        listenerITagLeido = listener
    }


    fun LeerTags() {
        try {
            ChainwaySDK.listenerITagLeido!!.estado(Configuracion.ESTADOS_LECTURA.INICIADO)

            val listaTags = mUhfrManager!!.tagInventoryRealTime()
            mUhfrManager!!.asyncStopReading()
            if (listaTags.isNotEmpty()) {
                var listTagsLeidos: MutableList<TagRFID> = ArrayList<TagRFID>()

                for (tag in listaTags) {
                    var tagRFID = TagRFID(
                        TID = Tools.Bytes2HexString(
                            tag.EmbededData,
                            tag.EmbededDatalen.toInt()
                        ),
                        EPC = "", //revisar como se obtiene
                        RSSI = tag.RSSI.toString(),
                        Count = 1
                    )

                    val tagExistente = listTagsLeidos.find { it.TID == tagRFID.TID }

                    if (tagExistente != null) {
                        tagExistente.Count += 1
                    } else {
                        listTagsLeidos.add(tagRFID)
                    }


                }
                listenerITagLeido!!.tagsLeidos(
                    listTagsLeidos
                )
                ChainwaySDK.listenerITagLeido!!.estado(Configuracion.ESTADOS_LECTURA.DETENIDA)
            } else {

                listenerITagLeido!!.error("No se pudo leer ningún tag RFID. Leer nuevamente.")
                ChainwaySDK.listenerITagLeido!!.estado(Configuracion.ESTADOS_LECTURA.ERROR)

            }
        } catch (ex: Exception) {
            ChainwaySDK.leyendo = false
            Log.e("Error en run", "$ex")
            ChainwaySDK.listenerITagLeido!!.estado(Configuracion.ESTADOS_LECTURA.ERROR)
            ChainwaySDK.listenerITagLeido!!.error("Error de lectura " + ex.message.toString())
        }


    }

    fun CerrarLectura() {
        if (mUhfrManager != null) {
            mUhfrManager!!.close()
            mUhfrManager = null
        }

    }

}