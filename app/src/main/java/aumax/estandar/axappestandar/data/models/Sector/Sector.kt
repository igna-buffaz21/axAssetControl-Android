package aumax.estandar.axappestandar.data.models.Sector

data class Sector(
    val id: Int,
    val idLocation: Int,
    val name: String,
    val idEmpresa: Int,
    val version: Int,
    val status: Boolean
)

/*

  {
    "id": 1006,
    "idLocation": 29,
    "name": "Primer Piso",
    "tagRfid": null,
    "idLocationNavigation": null,
    "subsectors": [],
    "idEmpresa": 1,
    "company": null,
    "version": 1,
    "status": true
  },

 */
