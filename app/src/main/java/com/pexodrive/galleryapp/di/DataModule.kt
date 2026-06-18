package com.pexodrive.galleryapp.di

import com.pexodrive.galleryapp.data.repository.MediaRepositoryImpl
import com.pexodrive.galleryapp.data.source.MediaStoreDataSource
import com.pexodrive.galleryapp.data.source.MediaStoreDataSourceImpl
import com.pexodrive.galleryapp.domain.repository.MediaRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindMediaStoreDataSource(
        impl: MediaStoreDataSourceImpl
    ): MediaStoreDataSource

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository
}
