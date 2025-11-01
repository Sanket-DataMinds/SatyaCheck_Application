package com.satyacheck.android.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.satyacheck.android.BuildConfig
import com.satyacheck.android.data.remote.ApiConstants
import com.satyacheck.android.data.remote.api.*
import com.satyacheck.android.utils.NetworkCacheInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Optimized network module for efficient network operations
 * Implements connection pooling, caching, and timeout handling
 */
@Module
@InstallIn(SingletonComponent::class)
object OptimizedNetworkModule {

    private const val CACHE_SIZE = 20 * 1024 * 1024L // 20 MB
    private const val CONNECTION_TIMEOUT = 15L // seconds
    private const val READ_TIMEOUT = 30L // seconds
    private const val WRITE_TIMEOUT = 30L // seconds
    private const val MAX_IDLE_CONNECTIONS = 5
    private const val KEEP_ALIVE_DURATION = 5L // minutes
    private const val MAX_REQUESTS_PER_HOST = 10
    private const val MAX_REQUESTS = 20

    /**
     * Provides a singleton OkHttpClient with optimized settings
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        cache: Cache,
        networkCacheInterceptor: NetworkCacheInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        // Create an optimized dispatcher for network requests
        val dispatcher = Dispatcher().apply {
            maxRequestsPerHost = MAX_REQUESTS_PER_HOST
            maxRequests = MAX_REQUESTS
        }

        // Create a connection pool for efficient connection reuse
        val connectionPool = ConnectionPool(
            MAX_IDLE_CONNECTIONS,
            KEEP_ALIVE_DURATION,
            TimeUnit.MINUTES
        )

        // Configure logging only for debug builds
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectionPool(connectionPool)
            .cache(cache)
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor) // Add auth interceptor first
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(networkCacheInterceptor) // Add our custom cache interceptor
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provides an HTTP cache for efficient network operations
     */
    @Provides
    @Singleton
    fun provideOkHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, CACHE_SIZE)
    }
    
    /**
     * Provides the network cache interceptor
     */
    @Provides
    @Singleton
    fun provideNetworkCacheInterceptor(@ApplicationContext context: Context): NetworkCacheInterceptor {
        return NetworkCacheInterceptor(context)
    }

    /**
     * Provides a Gson instance for JSON parsing
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Provides a base Retrofit instance with optimized settings
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * API Service providers
     */
    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAnalysisService(retrofit: Retrofit): AnalysisService {
        return retrofit.create(AnalysisService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideCommunityService(retrofit: Retrofit): CommunityService {
        return retrofit.create(CommunityService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideEducationalService(retrofit: Retrofit): EducationalService {
        return retrofit.create(EducationalService::class.java)
    }
}
