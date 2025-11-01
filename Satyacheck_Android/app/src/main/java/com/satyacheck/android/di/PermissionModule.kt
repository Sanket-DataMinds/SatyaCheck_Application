package com.satyacheck.android.di

import android.content.Context
import com.satyacheck.android.utils.PermissionHelpers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection module for permission utilities
 */
@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {
    
    @Provides
    @Singleton
    fun providePermissionHelpers(@ApplicationContext context: Context): PermissionHelpers {
        return PermissionHelpers(context)
    }
}
