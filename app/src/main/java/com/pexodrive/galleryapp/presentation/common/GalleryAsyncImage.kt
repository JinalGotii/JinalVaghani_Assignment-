package com.pexodrive.galleryapp.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale

enum class GalleryImageLoadStyle {
    Thumbnail,
    Viewer
}

@Composable
fun GalleryAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    loadStyle: GalleryImageLoadStyle = GalleryImageLoadStyle.Thumbnail,
    thumbnailSizePx: Int? = null
) {
    val context = LocalContext.current
    val placeholderColor = when (loadStyle) {
        GalleryImageLoadStyle.Thumbnail -> MaterialTheme.colorScheme.surfaceVariant
        GalleryImageLoadStyle.Viewer -> Color.Black
    }

    val imageRequest = remember(model, loadStyle, thumbnailSizePx) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(false)
            .apply {
                if (loadStyle == GalleryImageLoadStyle.Thumbnail && thumbnailSizePx != null) {
                    size(thumbnailSizePx)
                    scale(Scale.FILL)
                }
            }
            .build()
    }

    when (loadStyle) {
        GalleryImageLoadStyle.Thumbnail -> {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                loading = {
                    GalleryImagePlaceholder(modifier = Modifier.fillMaxSize())
                },
                error = {
                    GalleryImageError(modifier = Modifier.fillMaxSize())
                }
            )
        }
        GalleryImageLoadStyle.Viewer -> {
            AsyncImage(
                model = imageRequest,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                placeholder = ColorPainter(placeholderColor),
                error = ColorPainter(placeholderColor)
            )
        }
    }
}

@Composable
private fun GalleryImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun GalleryImageError(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun rememberGalleryThumbnailSizePx(): Int {
    val density = LocalDensity.current
    return remember(density) { with(density) { 200.dp.roundToPx() } }
}
