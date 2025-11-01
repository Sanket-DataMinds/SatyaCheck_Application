package com.satyacheck.android.di

import android.content.Context
import com.satyacheck.android.utils.EmbeddedServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServerModule {
    
    @Provides
    @Singleton
    fun provideEmbeddedServer(@ApplicationContext context: Context): EmbeddedServer {
        return EmbeddedServer(context)
    }
}