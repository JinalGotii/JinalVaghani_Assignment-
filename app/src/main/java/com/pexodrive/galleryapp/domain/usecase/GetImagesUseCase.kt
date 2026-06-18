package com.pexodrive.galleryapp.domain.usecase

import com.pexodrive.galleryapp.domain.model.MediaItem
import com.pexodrive.galleryapp.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetImagesUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<List<MediaItem>> = repository.getImages()

    fun peek(): List<MediaItem> = repository.peekImages()
}
