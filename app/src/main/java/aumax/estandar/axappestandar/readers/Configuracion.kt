package aumax.estandar.axappestandar.readers

object Configuracion {

    var potenciaRFID = 5
    var tiempoLecturaMs = 1000
    var tipoHandheld = 1

    object TIPO_HANDHELD{
        val ChainwayC72: Int = 1
        val Linkwin: Int = 2
    }

    object MODO_LECTURA{
        val RFID: Int = 1
        val Codigo: Int = 2
        val Ambas: Int = 3
    }

    object ESTADOS_LECTURA{
        val DETENIDA: Int = 0
        val INICIADO: Int = 1
        val LEYO: Int = 2
        val ERROR: Int = 3
    }

}