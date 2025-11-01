package com.satyacheck.android.di

import android.content.Context
import com.satyacheck.android.utils.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for asset management utilities
 */
@Module
@InstallIn(SingletonComponent::class)
object AssetModule {
    
    @Provides
    @Singleton
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager {
        return AssetManager(context)
    }
}
