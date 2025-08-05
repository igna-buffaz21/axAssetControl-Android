package aumax.estandar.axappestandar

import android.app.Application
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.data.remote.api.ActivoApiService
import aumax.estandar.axappestandar.data.remote.api.LocacionApiService
import aumax.estandar.axappestandar.data.remote.api.RegistroControlApiService
import aumax.estandar.axappestandar.data.remote.api.UserApiService
import aumax.estandar.axappestandar.data.remote.api.RetrofitClient
import aumax.estandar.axappestandar.data.remote.api.SectorApiService
import aumax.estandar.axappestandar.data.remote.api.SubSectorApiService

class MyApplication : Application() {
    companion object {
        lateinit var tokenManager: TokenManager
        lateinit var userApiService: UserApiService
        lateinit var subSectorApiService: SubSectorApiService
        lateinit var sectorApiService: SectorApiService
        lateinit var locacionApiService: LocacionApiService
        lateinit var activoApiService: ActivoApiService
        lateinit var registroControlApiService: RegistroControlApiService
    }

    override fun onCreate() {
        super.onCreate()

        tokenManager = TokenManager(this)

        val retrofit = RetrofitClient.create(tokenManager)

        userApiService = retrofit.create(UserApiService::class.java)
        subSectorApiService = retrofit.create(SubSectorApiService::class.java)
        sectorApiService = retrofit.create(SectorApiService::class.java)
        locacionApiService = retrofit.create(LocacionApiService::class.java)
        activoApiService = retrofit.create(ActivoApiService::class.java)
        registroControlApiService = retrofit.create(RegistroControlApiService::class.java)

    }
}