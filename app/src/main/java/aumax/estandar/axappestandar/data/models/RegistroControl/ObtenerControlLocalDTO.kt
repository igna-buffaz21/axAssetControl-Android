package aumax.estandar.axappestandar.data.models.RegistroControl

data class ObtenerControlLocalDTO(
    val id: Int,
    val id_subsector: Int,
    val date: Long,
    val name: String,
    val id_company: Int
)
