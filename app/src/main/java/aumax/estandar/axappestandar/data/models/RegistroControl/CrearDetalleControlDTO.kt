package aumax.estandar.axappestandar.data.models.RegistroControl

data class CrearDetalleControlDTO(
    val idControl: Int,
    val idActivo: Int,
    val status: String,
    val idAuditor: Int
)
