package com.pexodrive.galleryapp.data.repository

import com.pexodrive.galleryapp.data.source.MediaStoreDataSource
import com.pexodrive.galleryapp.domain.model.MediaItem
import com.pexodrive.galleryapp.domain.repository.MediaRepository
import com.pexodrive.galleryapp.utils.PreferencesManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaStoreDataSource: MediaStoreDataSource,
    private val preferencesManager: PreferencesManager
) : MediaRepository {

    override fun getImages(): Flow<List<MediaItem>> =
        mediaStoreDataSource.observeImages()

    override fun peekImages(): List<MediaItem> =
        mediaStoreDataSource.currentImages()

    override suspend fun refreshImages(): List<MediaItem> =
        mediaStoreDataSource.loadImages()

    override suspend fun deleteImages(items: List<MediaItem>): Result<Unit> =
        mediaStoreDataSource.deleteImages(items)

    override fun getGridColumnCount(): Flow<Int> =
        preferencesManager.gridColumnCount

    override suspend fun setGridColumnCount(columns: Int) {
        preferencesManager.setGridColumnCount(columns)
    }
}
