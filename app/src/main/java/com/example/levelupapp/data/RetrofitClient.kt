package com.example.levelupapp.data

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object RetrofitClient {
    private const val BASE_URL = "http://176.108.254.255"
    private var context: Context? = null

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    val apiService: ApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(TokenInterceptor())
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private class TokenInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
            var request = chain.request()
            val context = RetrofitClient.context ?: return@runBlocking chain.proceed(request)

            val accessToken = TokenManager.getAccessToken(context)
            if (accessToken != null) {
                request = request.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            }

            var response = chain.proceed(request)
            if (response.code == 401) {
                val retryTag = request.tag()
                if (retryTag == null || retryTag !is Boolean || !retryTag) {
                    val newToken = refreshToken()
                    request = request.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .tag(true)
                        .build()
                    response = chain.proceed(request)
                }
            }
            response
        }

        private suspend fun refreshToken(): String {
            val context = RetrofitClient.context ?: throw IllegalStateException("Контекст не инициализирован")
            val refreshToken = TokenManager.getRefreshToken(context)
                ?: throw Exception("Отсутствует refresh-токен")
            val response = RetrofitClient.apiService.refreshToken(RefreshTokenRequest(refreshToken))
            if (response.isSuccessful) {
                val newAccessToken = response.body()?.access ?: throw Exception("Отсутствует access-токен в ответе")
                TokenManager.saveAccessToken(context, newAccessToken)
                return newAccessToken
            } else {
                TokenManager.removeTokens(context)
                throw Exception("Не удалось обновить токен")
            }
        }
    }

    private suspend fun refreshToken(): String {
        val context = context ?: throw IllegalStateException("Контекст не инициализирован")
        val refreshToken = TokenManager.getRefreshToken(context)
            ?: throw Exception("Отсутствует refresh-токен")
        val response = withContext(Dispatchers.IO) {
            apiService.refreshToken(RefreshTokenRequest(refreshToken))
        }
        if (response.isSuccessful) {
            val newAccessToken = response.body()?.access ?: throw Exception("Отсутствует access-токен в ответе")
            TokenManager.saveAccessToken(context, newAccessToken)
            return newAccessToken
        } else {
            TokenManager.removeTokens(context)
            throw Exception("Не удалось обновить токен")
        }
    }
}

