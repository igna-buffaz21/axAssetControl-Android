package aumax.estandar.axappestandar.interfaces

interface ICodigoLeido {
    fun codigoLeido(codigo: String)
    fun error(mensaje: String)
    fun succes(estado: Int)

}