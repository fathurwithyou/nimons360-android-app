package com.eggheadengineers.nimons360.data.network

import com.eggheadengineers.nimons360.core.session.SessionManager
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://mad.labpro.hmif.dev/"

    private val json = Json { ignoreUnknownKeys = true }

    fun provideApiService(sessionManager: SessionManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(UnauthorizedInterceptor())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    fun provideWsClient(sessionManager: SessionManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun provideLiveStreamApi(): LiveStreamApiService {
        val client = buildLiveHttpClient()
        return Retrofit.Builder()
            .baseUrl(LiveConfig.COORDINATOR_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
            .build()
            .create(LiveStreamApiService::class.java)
    }

    fun provideLiveWsClient(): OkHttpClient = buildLiveHttpClient()

    private fun buildLiveHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)

        if (LiveConfig.API_KEY.isNotEmpty()) {
            builder.addInterceptor(Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("x-api-key", LiveConfig.API_KEY)
                    .build()
                chain.proceed(req)
            })
        }
        return builder.build()
    }
}
