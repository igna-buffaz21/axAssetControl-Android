package aumax.estandar.axappestandar.data.models.Activos

data class ObtenerActivosOSSDTO(
    val id: Int,
    val idSubsector: Int,
    val name: String,
    val tagRfid: String,
    val idSubsectorNavigation: SubSectorDTO
)

data class SubSectorDTO(
    val id: Int,
    val name: String,
    val tagRfid: String?
)
