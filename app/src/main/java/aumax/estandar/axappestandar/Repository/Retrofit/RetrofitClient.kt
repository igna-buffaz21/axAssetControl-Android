package aumax.estandar.axappestandar.Repository.Retrofit

import aumax.estandar.axappestandar.Data.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import aumax.estandar.axappestandar.utils.Constantes.ipAPI

object RetrofitClient {
    fun create(tokenManager: TokenManager): ApiService {
        val authInterceptor = Interceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://${ipAPI}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}