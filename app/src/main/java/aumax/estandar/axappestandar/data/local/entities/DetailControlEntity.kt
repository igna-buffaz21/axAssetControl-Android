package aumax.estandar.axappestandar.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detail_control",
    foreignKeys = [
        ForeignKey(
            entity = ControlRecord::class,
            parentColumns = ["id"],
            childColumns = ["id_control"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Active::class,
            parentColumns = ["id"],
            childColumns = ["id_activo"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_control"]),
        Index(value = ["id_activo"])
    ]
)
data class DetailControl(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_control: Int,
    val id_activo: Int,
    val status: Int,
    val id_auditor: Int,
    val sync: Boolean
)

