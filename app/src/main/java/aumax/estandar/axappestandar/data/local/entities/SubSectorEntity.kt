package aumax.estandar.axappestandar.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subsector")
data class SubSector(
    @PrimaryKey val id: Int,
    val id_sector: Int,
    val name: String,
    val tag_rfid: String,
    val id_company: Int,
    val version: Int,
    val status: Boolean
)
