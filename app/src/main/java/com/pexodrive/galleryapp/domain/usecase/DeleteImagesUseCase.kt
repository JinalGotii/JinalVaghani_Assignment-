package com.pexodrive.galleryapp.domain.usecase

import com.pexodrive.galleryapp.domain.model.MediaItem
import com.pexodrive.galleryapp.domain.repository.MediaRepository
import javax.inject.Inject

class DeleteImagesUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(items: List<MediaItem>): Result<Unit> =
        repository.deleteImages(items)
}
