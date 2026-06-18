package com.pexodrive.galleryapp.domain.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long,
    val dateModified: Long,
    val mimeType: String,
    val size: Long
)
