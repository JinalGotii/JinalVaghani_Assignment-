package com.pexodrive.galleryapp.data.source

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.pexodrive.galleryapp.domain.model.MediaItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaStoreDataSource {

    private val contentResolver: ContentResolver = context.contentResolver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val imagesState = MutableStateFlow<List<MediaItem>>(emptyList())
    private var contentObserver: ContentObserver? = null

    override fun currentImages(): List<MediaItem> = imagesState.value

    override fun observeImages(): Flow<List<MediaItem>> = imagesState.asStateFlow().onSubscription {
        registerContentObserverIfNeeded()
        if (imagesState.value.isEmpty()) {
            scope.launch { loadImages() }
        }
    }

    override suspend fun loadImages(): List<MediaItem> = withContext(Dispatchers.IO) {
        val images = queryImages()
        imagesState.value = images
        images
    }

    override suspend fun deleteImages(items: List<MediaItem>): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                items.forEach { item ->
                    val deleted = contentResolver.delete(item.uri, null, null)
                    if (deleted <= 0) {
                        return@withContext Result.failure(
                            Exception("Failed to delete ${item.displayName}")
                        )
                    }
                }
                imagesState.value = imagesState.value.filter { cached ->
                    items.none { it.id == cached.id }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun registerContentObserverIfNeeded() {
        if (contentObserver != null) return

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                scope.launch { loadImages() }
            }
        }.also { observer ->
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
        }
    }

    private fun queryImages(): List<MediaItem> {
        val images = mutableListOf<MediaItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        displayName = cursor.getString(nameColumn) ?: "",
                        dateAdded = cursor.getLong(dateAddedColumn),
                        dateModified = cursor.getLong(dateModifiedColumn),
                        mimeType = cursor.getString(mimeTypeColumn) ?: "image/*",
                        size = cursor.getLong(sizeColumn)
                    )
                )
            }
        }
        return images
    }
}
