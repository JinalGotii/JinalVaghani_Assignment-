package com.pexodrive.galleryapp.data.source

import com.pexodrive.galleryapp.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaStoreDataSource {
    fun observeImages(): Flow<List<MediaItem>>
    fun currentImages(): List<MediaItem>
    suspend fun loadImages(): List<MediaItem>
    suspend fun deleteImages(items: List<MediaItem>): Result<Unit>
}
