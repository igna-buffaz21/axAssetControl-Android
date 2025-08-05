package aumax.estandar.axappestandar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.local.entities.SubSector

@Dao
interface ActivoDAO {
    @Query("SELECT * FROM active WHERE id_subsector = :id")
    suspend fun obtenerTodos(id: Int): List<Active>
}