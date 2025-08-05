package aumax.estandar.axappestandar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "control_record",
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
data class ControlRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_subsector: Int,
    val date: Int,
    val status: Int,
    val id_company: Int,
    val sync: Boolean
)

