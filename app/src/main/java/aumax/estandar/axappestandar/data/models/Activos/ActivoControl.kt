package aumax.estandar.axappestandar.data.models.Activos

data class ActivoControl(
    val id: Int,
    val idSubsector: Int,
    val name: String,
    val brand: String,
    val model: String,
    val seriaNumber: String,
    val tagRfid: String?,
    val idEmpresa: Int,
    val version: Int,
    val status: Boolean,
    val encontrado: String?
)
