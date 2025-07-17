package aumax.estandar.axappestandar.utils.interfaces

interface ICodigoLeido {
    fun codigoLeido(codigo: String)
    fun error(mensaje: String)
    fun succes(estado: Int)

}