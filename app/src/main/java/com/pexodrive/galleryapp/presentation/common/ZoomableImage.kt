package com.pexodrive.galleryapp.presentation.common

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import com.pexodrive.galleryapp.domain.model.MediaItem
import kotlin.math.hypot

private const val MIN_ZOOM_SCALE = 1f
private const val MAX_ZOOM_SCALE = 5f
private const val DOUBLE_TAP_ZOOM_SCALE = 2.5f
private const val ZOOMED_THRESHOLD = 1.01f

@Composable
fun ZoomableImage(
    item: MediaItem,
    modifier: Modifier = Modifier,
    onZoomedChange: (Boolean) -> Unit = {}
) {
    val thumbnailSize = rememberGalleryThumbnailSizePx()
    var scale by remember(item.id) { mutableFloatStateOf(MIN_ZOOM_SCALE) }
    var offset by remember(item.id) { mutableStateOf(Offset.Zero) }
    var containerSize by remember(item.id) { mutableStateOf(Size.Zero) }

    LaunchedEffect(scale) {
        onZoomedChange(scale > ZOOMED_THRESHOLD)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it.toSize() }
            .pointerInput(item.id) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > ZOOMED_THRESHOLD) {
                            scale = MIN_ZOOM_SCALE
                            offset = Offset.Zero
                        } else {
                            scale = DOUBLE_TAP_ZOOM_SCALE
                            offset = zoomOffsetForFocalPoint(
                                focalPoint = tapOffset,
                                containerSize = containerSize,
                                targetScale = DOUBLE_TAP_ZOOM_SCALE
                            )
                        }
                    }
                )
            }
            .pointerInput(item.id) {
                detectZoomPanGestures(
                    getScale = { scale },
                    onZoom = { centroid, zoom ->
                        val (newScale, newOffset) = applyZoom(
                            scale = scale,
                            offset = offset,
                            centroid = centroid,
                            zoom = zoom,
                            containerSize = containerSize
                        )
                        scale = newScale
                        offset = newOffset
                    },
                    onPan = { pan ->
                        if (scale > ZOOMED_THRESHOLD) {
                            offset = constrainOffset(offset + pan, scale, containerSize)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        GalleryAsyncImage(
            model = item.uri,
            contentDescription = item.displayName,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                    transformOrigin = TransformOrigin.Center
                },
            contentScale = ContentScale.Fit,
            loadStyle = GalleryImageLoadStyle.Viewer,
            thumbnailSizePx = thumbnailSize
        )
    }
}

private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectZoomPanGestures(
    getScale: () -> Float,
    onZoom: (centroid: Offset, zoom: Float) -> Unit,
    onPan: (pan: Offset) -> Unit
) {
    val touchSlop = viewConfiguration.touchSlop

    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var accumulatedPan = Offset.Zero

        do {
            val event = awaitPointerEvent(PointerEventPass.Main)
            if (event.changes.none { it.pressed }) break

            val pressedCount = event.changes.count { it.pressed }
            val zoomChange = event.calculateZoom()
            val panChange = event.calculatePan()
            val centroid = event.calculateCentroid(useCurrent = true)
            val isZoomed = getScale() > ZOOMED_THRESHOLD

            when {
                pressedCount >= 2 -> {
                    onZoom(centroid, zoomChange)
                    event.changes.forEach { it.consume() }
                }
                isZoomed -> {
                    onPan(panChange)
                    event.changes.forEach { it.consume() }
                }
                else -> {
                    accumulatedPan += panChange
                    if (hypot(accumulatedPan.x, accumulatedPan.y) > touchSlop) {
                        return@awaitEachGesture
                    }
                }
            }
        } while (true)
    }
}

private fun applyZoom(
    scale: Float,
    offset: Offset,
    centroid: Offset,
    zoom: Float,
    containerSize: Size
): Pair<Float, Offset> {
    val newScale = (scale * zoom).coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
    if (newScale <= MIN_ZOOM_SCALE) {
        return MIN_ZOOM_SCALE to Offset.Zero
    }

    val center = containerCenter(containerSize)
    val newOffset = offset + (centroid - center) * (scale - newScale)
    return newScale to constrainOffset(newOffset, newScale, containerSize)
}

private fun constrainOffset(offset: Offset, scale: Float, containerSize: Size): Offset {
    if (containerSize == Size.Zero || scale <= MIN_ZOOM_SCALE) {
        return Offset.Zero
    }

    val maxX = containerSize.width * (scale - 1f) / 2f
    val maxY = containerSize.height * (scale - 1f) / 2f
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}

private fun zoomOffsetForFocalPoint(
    focalPoint: Offset,
    containerSize: Size,
    targetScale: Float
): Offset {
    if (containerSize == Size.Zero) return Offset.Zero

    val center = containerCenter(containerSize)
    val unconstrained = (focalPoint - center) * (1f - targetScale)
    return constrainOffset(unconstrained, targetScale, containerSize)
}

private fun containerCenter(containerSize: Size): Offset {
    return Offset(containerSize.width / 2f, containerSize.height / 2f)
}
