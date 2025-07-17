package aumax.estandar.axappestandar.data.remote.api

import aumax.estandar.axappestandar.data.remote.interceptors.Interceptor
import aumax.estandar.axappestandar.data.local.TokenManager
import aumax.estandar.axappestandar.utils.Constantes.apiURL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun create(tokenManager: TokenManager): Retrofit {
        val authInterceptor = Interceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(apiURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}