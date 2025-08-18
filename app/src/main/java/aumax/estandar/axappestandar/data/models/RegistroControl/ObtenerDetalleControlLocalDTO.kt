package aumax.estandar.axappestandar.data.models.RegistroControl

data class ObtenerDetalleControlLocalDTO(
    val id: Int,
    val id_control: Int,
    val id_activo: Int,
    val status: String,
    val id_auditor: Int,
    val sync: Int,
    val name: String,
)
