package com.pexodrive.galleryapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pexodrive.galleryapp.domain.model.MediaPermissionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getPermissionStatus(): MediaPermissionStatus {
        return when {
            hasFullMediaAccess() -> MediaPermissionStatus.GRANTED
            hasPartialMediaAccess() -> MediaPermissionStatus.PARTIAL
            else -> MediaPermissionStatus.DENIED
        }
    }

    /**
     * Permissions requested for full library access (scoped storage compliant).
     * API 33+: READ_MEDIA_IMAGES
     * API 24–32: READ_EXTERNAL_STORAGE (maxSdkVersion 32 in manifest)
     *
     * API 34+ partial access (READ_MEDIA_VISUAL_USER_SELECTED) is granted by the system
     * when the user picks "Selected photos" — it is not requested directly.
     */
    fun getFullAccessPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /** @deprecated Use [getFullAccessPermissions] */
    fun getRequiredPermissions(): Array<String> = getFullAccessPermissions()

    fun supportsPartialPhotoAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    fun hasFullMediaAccess(): Boolean {
        return getFullAccessPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPartialMediaAccess(): Boolean {
        if (!supportsPartialPhotoAccess()) return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED && !hasFullMediaAccess()
    }

    fun hasAnyMediaAccess(): Boolean = hasFullMediaAccess() || hasPartialMediaAccess()

    fun canShowRationale(activity: Activity): Boolean {
        return getFullAccessPermissions().any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    fun isPermanentlyDenied(wasDeniedOnce: Boolean, canShowRationale: Boolean): Boolean {
        return wasDeniedOnce && !canShowRationale
    }

    fun createAppSettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
