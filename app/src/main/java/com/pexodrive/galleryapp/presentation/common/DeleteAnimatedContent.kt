package com.pexodrive.galleryapp.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.pexodrive.galleryapp.utils.Constants

@Composable
fun DeleteAnimatedContent(
    isDeleting: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (isDeleting) 0f else 1f,
        animationSpec = tween(Constants.DELETE_ANIMATION_DURATION_MS),
        label = "deleteAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isDeleting) 0.85f else 1f,
        animationSpec = tween(Constants.DELETE_ANIMATION_DURATION_MS),
        label = "deleteScale"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}
