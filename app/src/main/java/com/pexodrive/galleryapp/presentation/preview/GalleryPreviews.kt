package com.pexodrive.galleryapp.presentation.preview

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.pexodrive.galleryapp.R
import com.pexodrive.galleryapp.domain.model.MediaItem
import com.pexodrive.galleryapp.domain.model.MediaPermissionStatus
import com.pexodrive.galleryapp.presentation.common.MediaGridItem
import com.pexodrive.galleryapp.presentation.common.PartialAccessBanner
import com.pexodrive.galleryapp.presentation.grid.GridEmptyState
import com.pexodrive.galleryapp.presentation.grid.GridScreenContent
import com.pexodrive.galleryapp.presentation.grid.GridUiState
import com.pexodrive.galleryapp.presentation.permission.DeniedRationaleCard
import com.pexodrive.galleryapp.presentation.permission.PartialAccessInfoCard


import com.pexodrive.galleryapp.presentation.permission.PermissionScreenContent
import com.pexodrive.galleryapp.presentation.permission.PermissionUiState
import com.pexodrive.galleryapp.presentation.viewer.ViewerScreenContent
import com.pexodrive.galleryapp.ui.theme.GalleryAppTheme

private val previewImages = List(12) { index ->
    MediaItem(
        id = index.toLong(),
        uri = Uri.parse("content://media/external/images/media/$index"),
        displayName = "photo_$index.jpg",
        dateAdded = 1_700_000_000L + index,
        dateModified = 1_700_000_000L + index,
        mimeType = "image/jpeg",
        size = 1024L * 512
    )
}

private val previewNoop: () -> Unit = {}


@PreviewScreenSizes
@Preview(name = "Permission – Initial", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PermissionScreenInitialPreview() {
    PermissionScreenContent(
        uiState = PermissionUiState(isLoading = false),
        onPermissionGranted = previewNoop,
        onRequestPermission = previewNoop,
        onGrantFullAccess = previewNoop,
        onGoToSettings = previewNoop
    )
}

@Preview(name = "Permission – Partial access", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PermissionScreenPartialPreview() {
    PermissionScreenContent(
        uiState = PermissionUiState(
            status = MediaPermissionStatus.PARTIAL,
            accessibleImageCount = 8,
            isLoading = false
        ),
        onPermissionGranted = previewNoop,
        onRequestPermission = previewNoop,
        onGrantFullAccess = previewNoop,
        onGoToSettings = previewNoop
    )
}

@Preview(name = "Permission – Denied", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PermissionScreenDeniedPreview() {
    PermissionScreenContent(
        uiState = PermissionUiState(
            status = MediaPermissionStatus.DENIED,
            wasDeniedOnce = true,
            isLoading = false
        ),
        onPermissionGranted = previewNoop,
        onRequestPermission = previewNoop,
        onGrantFullAccess = previewNoop,
        onGoToSettings = previewNoop
    )
}

@Preview(name = "Permission – Granted", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PermissionScreenGrantedPreview() {
    PermissionScreenContent(
        uiState = PermissionUiState(
            status = MediaPermissionStatus.GRANTED,
            isLoading = false
        ),
        onPermissionGranted = previewNoop,
        onRequestPermission = previewNoop,
        onGrantFullAccess = previewNoop,
        onGoToSettings = previewNoop
    )
}


@Preview(name = "Partial Access Card", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun PartialAccessInfoCardPreview() {
    PartialAccessInfoCard(
        imageCount = 8,
        onGrantFullAccess = previewNoop,
        modifier = Modifier.padding(16.dp)
    )
}

@Preview(name = "Denied Rationale Card", showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun DeniedRationaleCardPreview() {
    DeniedRationaleCard(
        isPermanentlyDenied = false,
        onGoToSettings = previewNoop,
        modifier = Modifier.padding(16.dp)
    )
}

@PreviewScreenSizes
@Preview(name = "Grid – With images", showBackground = true)
@Composable
private fun GridScreenPreview() {
    GalleryAppTheme {
        GridScreenContent(
            uiState = GridUiState(
                images = previewImages,
                columnCount = 3
            ),
            snackbarHostState = SnackbarHostState(),
            onToggleColumns = {},
            onClearSelection = previewNoop,
            onShareSelected = previewNoop,
            onShowDeleteDialog = previewNoop,
            onRefresh = previewNoop,
            onImageClick = {},
            onImageLongClick = {},
            onGrantFullAccess = previewNoop
        )
    }
}

@Preview(name = "Grid – Selection mode", showBackground = true)
@Composable
private fun GridScreenSelectionPreview() {
    GalleryAppTheme {
        GridScreenContent(
            uiState = GridUiState(
                images = previewImages,
                columnCount = 3,
                isSelectionMode = true,
                selectedIds = setOf(0L, 2L, 5L)
            ),
            snackbarHostState = SnackbarHostState(),
            onToggleColumns = {},
            onClearSelection = previewNoop,
            onShareSelected = previewNoop,
            onShowDeleteDialog = previewNoop,
            onRefresh = previewNoop,
            onImageClick = {},
            onImageLongClick = {},
            onGrantFullAccess = previewNoop
        )
    }
}

@Preview(name = "Grid – Partial access banner", showBackground = true)
@Composable
private fun GridPartialAccessPreview() {
    GalleryAppTheme {
        GridScreenContent(
            uiState = GridUiState(
                images = previewImages.take(4),
                columnCount = 2,
                showPartialAccessBanner = true
            ),
            snackbarHostState = SnackbarHostState(),
            onToggleColumns = {},
            onClearSelection = previewNoop,
            onShareSelected = previewNoop,
            onShowDeleteDialog = previewNoop,
            onRefresh = previewNoop,
            onImageClick = {},
            onImageLongClick = {},
            onGrantFullAccess = previewNoop
        )
    }
}

@Preview(name = "Grid – Empty", showBackground = true)
@Composable
private fun GridEmptyPreview() {
    GalleryAppTheme {
        GridEmptyState(modifier = Modifier.padding(32.dp))
    }
}

@Preview(name = "Partial Access Banner", showBackground = true)
@Composable
private fun PartialAccessBannerPreview() {
    GalleryAppTheme {
        PartialAccessBanner(
            imageCount = 12,
            onGrantFullAccess = previewNoop
        )
    }
}

@Preview(name = "Media Grid Item", showBackground = true)
@Composable
private fun MediaGridItemPreview() {
    GalleryAppTheme {
        MediaGridItem(
            item = previewImages.first(),
            isSelected = false,
            onClick = previewNoop,
            onLongClick = previewNoop,
            modifier = Modifier.size(120.dp)
        )
    }
}

@Preview(name = "Media Grid Item – Selected", showBackground = true)
@Composable
private fun MediaGridItemSelectedPreview() {
    GalleryAppTheme {
        MediaGridItem(
            item = previewImages.first(),
            isSelected = true,
            onClick = previewNoop,
            onLongClick = previewNoop,
            modifier = Modifier.size(120.dp)
        )
    }
}

@PreviewScreenSizes
@Preview(name = "Viewer", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun ViewerScreenPreview() {
    ViewerScreenContent(
        images = previewImages,
        startIndex = 2,
        deletingIds = emptySet(),
        onBack = previewNoop,
        onShare = {},
        onDelete = {}
    )
}
