package aumax.estandar.axappestandar.data.models.SubSector

data class SubSector(
    val id: Int,
    val idSector: Int,
    val name: String,
    val tagRfid: String,
    val idEmpresa: Int,
    val version: Int,
    val status: Boolean
)

/*

    "id": 6,
    "idSector": 1012,
    "name": "Deposito",
    "tagRfid": "1",
    "actives": [],
    "controlRecords": [],
    "idSectorNavigation": null,
    "idEmpresa": 1,
    "company": null,
    "version": 1,
    "status": true

 */