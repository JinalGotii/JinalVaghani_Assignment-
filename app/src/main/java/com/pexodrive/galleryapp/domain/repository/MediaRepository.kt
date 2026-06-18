package com.pexodrive.galleryapp.domain.repository

import com.pexodrive.galleryapp.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getImages(): Flow<List<MediaItem>>
    fun peekImages(): List<MediaItem>
    suspend fun refreshImages(): List<MediaItem>
    suspend fun deleteImages(items: List<MediaItem>): Result<Unit>
    fun getGridColumnCount(): Flow<Int>
    suspend fun setGridColumnCount(columns: Int)
}
