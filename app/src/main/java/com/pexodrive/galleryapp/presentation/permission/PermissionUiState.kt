package com.pexodrive.galleryapp.presentation.permission

import com.pexodrive.galleryapp.domain.model.MediaPermissionStatus

data class PermissionUiState(
    val status: MediaPermissionStatus = MediaPermissionStatus.NOT_REQUESTED,
    val wasDeniedOnce: Boolean = false,
    val isPermanentlyDenied: Boolean = false,
    val accessibleImageCount: Int = 0,
    val isLoading: Boolean = true
) {
    val showPartialAccessInfo: Boolean
        get() = status == MediaPermissionStatus.PARTIAL

    val showDeniedRationale: Boolean
        get() = status == MediaPermissionStatus.DENIED

    val showNotRequestedHint: Boolean
        get() = status == MediaPermissionStatus.NOT_REQUESTED

    val canContinue: Boolean
        get() = status == MediaPermissionStatus.GRANTED ||
            status == MediaPermissionStatus.PARTIAL
}
