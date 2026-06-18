package com.pexodrive.galleryapp.utils

import android.content.ContentResolver
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import com.pexodrive.galleryapp.domain.model.MediaItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteHelper @Inject constructor(
    private val contentResolver: ContentResolver
) {
    fun requiresSystemConfirmation(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun createDeleteRequest(items: List<MediaItem>): IntentSenderRequest? {
        if (!requiresSystemConfirmation()) return null
        val pendingIntent = MediaStore.createDeleteRequest(
            contentResolver,
            items.map { it.uri }
        )
        return IntentSenderRequest.Builder(pendingIntent.intentSender).build()
    }
}
