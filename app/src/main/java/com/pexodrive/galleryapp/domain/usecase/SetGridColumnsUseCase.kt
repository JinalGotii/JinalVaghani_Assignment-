package com.pexodrive.galleryapp.domain.usecase

import com.pexodrive.galleryapp.domain.repository.MediaRepository
import javax.inject.Inject

class SetGridColumnsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(columns: Int) = repository.setGridColumnCount(columns)
}
