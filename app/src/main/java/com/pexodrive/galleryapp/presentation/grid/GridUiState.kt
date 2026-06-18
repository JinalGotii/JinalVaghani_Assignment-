package com.pexodrive.galleryapp.presentation.grid

import android.content.Intent
import androidx.activity.result.IntentSenderRequest
import com.pexodrive.galleryapp.domain.model.MediaItem

data class GridUiState(
    val images: List<MediaItem> = emptyList(),
    val columnCount: Int = 3,
    val isRefreshing: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val showPartialAccessBanner: Boolean = false,
    val pendingDeleteItems: List<MediaItem>? = null,
    val deletingIds: Set<Long> = emptySet()
)

sealed interface GridEvent {
    data class Share(val intent: Intent) : GridEvent
    data class RequestDeleteConfirmation(val request: IntentSenderRequest) : GridEvent
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : GridEvent
}
