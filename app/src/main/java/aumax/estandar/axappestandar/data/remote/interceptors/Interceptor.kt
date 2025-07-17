package aumax.estandar.axappestandar.data.remote.interceptors

import aumax.estandar.axappestandar.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class Interceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenManager.getToken()

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        else {
            chain.request()
        }
        return chain.proceed(request)
    }
}