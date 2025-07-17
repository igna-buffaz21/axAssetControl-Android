package aumax.estandar.axappestandar.data.models.Activos

data class Activo(
    val id: Int,
    val idSubsector: Int,
    val name: String,
    val brand: String,
    val model: String,
    val seriaNumber: String,
    val tagRfid: String,
    val idEmpresa: Int,
    val version: Int,
    val status: Boolean
)

/*

  {
    "id": 37,
    "idSubsector": 6,
    "name": "Silla ergonómica",
    "brand": "ErgoPlus",
    "model": "E400",
    "seriaNumber": "SN-ERG-6001",
    "tagRfid": "1",
    "idActiveType": 1,
    "detailControls": [],
    "idActiveTypeNavigation": null,
    "idSubsectorNavigation": null,
    "idEmpresa": 1,
    "company": null,
    "version": 1,
    "status": true
  },

 */
