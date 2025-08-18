package aumax.estandar.axappestandar.data

import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.models.Activos.Activo
import aumax.estandar.axappestandar.data.models.SubSector.SubSector

fun Activo.toEntity(): Active {
    return Active(
        id = this.id,
        id_subsector = this.idSubsector,
        name = this.name,
        brand = this.brand,
        model = this.model,
        seria_number = this.seriaNumber,
        tag_rfid = this.tagRfid ?: "",
        id_active_type = 1,
        id_company = this.idEmpresa,
        version = this.version,
        status = this.status
    )
}

fun Active.toEntity(): Activo {
    return Activo(
        id = this.id,
        idSubsector = this.id_subsector,
        name = this.name,
        brand = this.brand,
        model = this.model,
        seriaNumber = this.seria_number,
        tagRfid = this.tag_rfid ?: "",
        idEmpresa = this.id_company,
        version = this.version,
        status = this.status,
        encontrado = ""
    )
}

fun SubSector.toEntity() : aumax.estandar.axappestandar.data.local.entities.SubSector {
    return aumax.estandar.axappestandar.data.local.entities.SubSector(
        id = this.id,
        id_sector = this.idSector,
        name = this.name,
        tag_rfid = this.tagRfid,
        id_company = this.idEmpresa,
        version = this.version,
        status = this.status
    )
}

fun aumax.estandar.axappestandar.data.local.entities.SubSector.toModel() : SubSector {
    return SubSector(
        id = this.id,
        idSector = this.id_sector,
        name = this.name,
        tagRfid = this.tag_rfid,
        idEmpresa = this.id_company,
        version = this.version,
        status = this.status
    )
}

/*

    @PrimaryKey val id: Int,
    val id_subsector: Int,
    val name: String,
    val brand: String,
    val model: String,
    val seria_number: String,
    val tag_rfid: String,
    val id_active_type: Int,
    val id_company: Int,
    val version: Int,
    val status: Boolean

 */