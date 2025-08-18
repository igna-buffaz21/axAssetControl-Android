package aumax.estandar.axappestandar.data.models.RegistroControl

data class SincronizacionDTO(
    val controles: List<ObtenerControlLocalDTO>,
    val detalles: List<ObtenerDetalleControlLocalDTO>
)

