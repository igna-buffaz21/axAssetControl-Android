package aumax.estandar.axappestandar

import android.app.Application
import aumax.estandar.axappestandar.Data.TokenManager
import aumax.estandar.axappestandar.Repository.Retrofit.ApiService
import aumax.estandar.axappestandar.Repository.Retrofit.RetrofitClient

class MyApplication : Application() {
    companion object {
        lateinit var tokenManager: TokenManager
        lateinit var apiService: ApiService
    }

    override fun onCreate() {
        super.onCreate()

        tokenManager = TokenManager(this)
        apiService = RetrofitClient.create(tokenManager)
    }
}