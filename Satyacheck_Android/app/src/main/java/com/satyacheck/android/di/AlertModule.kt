package com.satyacheck.android.di

import com.satyacheck.android.data.repository.AlertRepositoryImpl
import com.satyacheck.android.domain.repository.AlertRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlertModule {
    
    @Binds
    @Singleton
    abstract fun bindAlertRepository(
        alertRepositoryImpl: AlertRepositoryImpl
    ): AlertRepository
}
