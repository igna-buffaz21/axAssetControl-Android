package aumax.estandar.axappestandar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.local.entities.SubSector
import aumax.estandar.axappestandar.utils.TagRFID

@Dao
interface SubSectorDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSubsector(subsector: SubSector) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActivos(activos: List<Active>) : List<Long>

    @Query("SELECT * FROM subsector WHERE id_company = :id")
    suspend fun obtenerTodos(id: Int): List<SubSector>

    @Transaction
    suspend fun descargarSubsectorYActivo(subsector: SubSector, activos: List<Active>) : Boolean {
        val subSector = insertarSubsector(subsector)
        val activos = insertarActivos(activos)

        return subSector != -1L && activos.isNotEmpty()
    }
}