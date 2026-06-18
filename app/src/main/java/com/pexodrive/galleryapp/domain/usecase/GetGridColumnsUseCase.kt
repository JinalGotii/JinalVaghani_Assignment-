package com.pexodrive.galleryapp.domain.usecase

import com.pexodrive.galleryapp.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGridColumnsUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Flow<Int> = repository.getGridColumnCount()
}
