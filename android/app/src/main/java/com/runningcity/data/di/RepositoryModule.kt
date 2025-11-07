package com.runningcity.data.di

import android.content.Context
import com.runningcity.data.location.LocationRepositoryImpl
import com.runningcity.data.network.LocationApiService
import com.runningcity.domain.repository.LocationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🧩 RepositoryModule
 * ────────────────────────────────────────────────
 * - LocationRepositoryImpl 을 Hilt에 등록
 * - LocationApiService 는 NetworkModule 에서 제공됨
 * ────────────────────────────────────────────────
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context,
        api: LocationApiService
    ): LocationRepository = LocationRepositoryImpl(context, api)
}
