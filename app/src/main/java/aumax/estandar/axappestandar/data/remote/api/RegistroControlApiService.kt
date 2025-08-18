package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.models.Activos.ResponseAsignarTagActivo
import aumax.estandar.axappestandar.data.models.RegistroControl.CrearDetalleControlDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.CrearRegistroControlResponseDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.RegistroControlDTO
import aumax.estandar.axappestandar.data.models.RegistroControl.SincronizacionDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistroControlApiService {
    @POST("api/RegistroControl")
    suspend fun registrarControl(@Body registroControl: RegistroControlDTO ) : Response<CrearRegistroControlResponseDTO>

    @POST("api/DetalleControl")
    suspend fun crearDetalleControl(@Body detalleControl: CrearDetalleControlDTO) : Response<ResponseAsignarTagActivo>

    @POST("api/DetalleControl/CrearDetalleControlEnCantidad")
    suspend fun CrearDetallesControlEnCantidad(@Body detallesControl: List<CrearDetalleControlDTO>) : Response<ResponseAsignarTagActivo>

    @POST("api/RegistroControl/SincronizarControlesYDetalles")
    suspend fun SincronizarControlesYDetalles(@Body sincronizacionDTO: SincronizacionDTO) : Response<ResponseAsignarTagActivo>
}