package aumax.estandar.axappestandar.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import aumax.estandar.axappestandar.data.local.entities.ControlRecord
import aumax.estandar.axappestandar.data.local.entities.DetailControl
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerControlLocalDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.ObtenerDetalleControlLocalDTO

@Dao
interface RegistroControlDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun crearControl(control: ControlRecord) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun crearDetalleControl(detalleControl: List<DetailControl>): List<Long>

    @Query("SELECT cr.id, cr.id_subsector, cr.date, ss.name \n" +
            "FROM control_record as cr \n" +
            "INNER JOIN subsector as ss ON cr.id_subsector = ss.id \n" +
            "WHERE cr.id_company = :id_company AND cr.sync = false AND sync = 0")
    suspend fun obtenerControles(id_company: Int) : List<ObtenerControlLocalDTO>

    @Query("SELECT dc.*, a.name FROM detail_control as dc \n" +
            "INNER JOIN active as a ON dc.id_activo = a.id\n" +
            "WHERE dc.id_control = :id_control AND sync = 0")
    suspend fun obtenerDetalleControlPorControl(id_control: Int) : List<ObtenerDetalleControlLocalDTO>

    @Query("UPDATE control_record SET sync = true WHERE sync = 0")
    suspend fun marcarAsyncControl()

    @Query("UPDATE detail_control SET sync = true WHERE sync = 0")
    suspend fun marcarAsyncDetailControl()
}