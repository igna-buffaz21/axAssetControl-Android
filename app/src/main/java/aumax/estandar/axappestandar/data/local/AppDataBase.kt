package aumax.estandar.axappestandar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import aumax.estandar.axappestandar.data.local.dao.ActivoDAO
import aumax.estandar.axappestandar.data.local.dao.SubSectorDAO
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.local.entities.ControlRecord
import aumax.estandar.axappestandar.data.local.entities.DetailControl
import aumax.estandar.axappestandar.data.local.entities.SubSector

@Database(
    entities = [SubSector::class, Active::class, ControlRecord::class, DetailControl::class],
    version = 5,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun subsectorDao(): SubSectorDAO
    abstract fun activoDao(): ActivoDAO
}