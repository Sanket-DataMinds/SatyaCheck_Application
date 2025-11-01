package com.satyacheck.android.di

import com.satyacheck.android.data.remote.ApiConstants
import com.satyacheck.android.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

// This module is disabled in favor of OptimizedNetworkModule
// @Module
// @InstallIn(SingletonComponent::class)
object NetworkModule {

    /*
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
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
    */
}
