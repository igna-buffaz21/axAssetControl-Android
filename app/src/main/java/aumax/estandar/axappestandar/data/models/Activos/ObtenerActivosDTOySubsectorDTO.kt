package aumax.estandar.axappestandar.data.models.Activos

data class RetornarActivosDTOySubsectorDTO(
    val activosDTO: List<Activo>,
    val subsector: String
)

data class ObtenerActivoDTO(
    val id: Int,
    val idSubsector: Int,
    val name: String,
    val brand: String,
    val model: String,
    val seriaNumber: String,
    val tagRfid: String?,
    val idActiveType: Int
)
