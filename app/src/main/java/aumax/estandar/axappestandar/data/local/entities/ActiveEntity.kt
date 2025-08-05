package aumax.estandar.axappestandar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "active",
    foreignKeys = [
        ForeignKey(
            entity = SubSector::class,
            parentColumns = ["id"],
            childColumns = ["id_subsector"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id_subsector"])]
)
data class Active(
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
)

