package aumax.estandar.axappestandar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import aumax.estandar.axappestandar.data.local.entities.Active
import aumax.estandar.axappestandar.data.local.entities.SubSector
import aumax.estandar.axappestandar.data.models.Activos.Activo

@Dao
interface ActivoDAO {
    @Query("SELECT * FROM active WHERE id_subsector = :id")
    suspend fun obtenerTodos(id: Int): List<Active>

    @Query("SELECT id FROM subsector WHERE tag_rfid = :tag_rfid AND id_company = :id_company")
    suspend fun obtenerIdSubsector(tag_rfid: String, id_company: Int) : Int

    @Query("SELECT * FROM active WHERE id_subsector = :id")
    suspend fun obtenerActivosBD(id: Int) : List<Active>
}