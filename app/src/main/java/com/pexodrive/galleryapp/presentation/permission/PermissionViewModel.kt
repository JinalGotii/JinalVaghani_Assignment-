package com.pexodrive.galleryapp.presentation.permission

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pexodrive.galleryapp.domain.model.MediaPermissionStatus
import com.pexodrive.galleryapp.domain.usecase.RefreshImagesUseCase
import com.pexodrive.galleryapp.utils.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val refreshImagesUseCase: RefreshImagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            syncUiWithPermissionState(canShowRationale = null)
        }
    }

    fun onRequestPermission(
        canShowRationale: Boolean,
        requestPermission: () -> Unit
    ) {
        if (permissionManager.hasAnyMediaAccess()) {
            refreshStatus()
            return
        }

        val state = _uiState.value
        if (permissionManager.isPermanentlyDenied(state.wasDeniedOnce, canShowRationale)) {
            _uiState.update {
                it.copy(
                    isPermanentlyDenied = true,
                    status = MediaPermissionStatus.DENIED,
                    wasDeniedOnce = true
                )
            }
            return
        }

        requestPermission()
    }

    /**
     * Called after the system permission dialog closes.
     * Re-checks actual grant state so API 34+ partial ("Selected photos") is detected even when
     * [READ_MEDIA_IMAGES] is not granted.
     */
    fun onPermissionResult(canShowRationale: Boolean) {
        viewModelScope.launch {
            syncUiWithPermissionState(canShowRationale = canShowRationale)
        }
    }

    fun getFullAccessPermissions(): Array<String> =
        permissionManager.getFullAccessPermissions()

    fun createSettingsIntent(): Intent = permissionManager.createAppSettingsIntent()

    private suspend fun syncUiWithPermissionState(canShowRationale: Boolean?) {
        when {
            permissionManager.hasFullMediaAccess() -> {
                val imageCount = loadAccessibleImageCount()
                _uiState.update {
                    it.copy(
                        status = MediaPermissionStatus.GRANTED,
                        accessibleImageCount = imageCount,
                        wasDeniedOnce = false,
                        isPermanentlyDenied = false,
                        isLoading = false
                    )
                }
            }
            permissionManager.hasPartialMediaAccess() -> {
                val imageCount = loadAccessibleImageCount()
                _uiState.update {
                    it.copy(
                        status = MediaPermissionStatus.PARTIAL,
                        accessibleImageCount = imageCount,
                        wasDeniedOnce = false,
                        isPermanentlyDenied = false,
                        isLoading = false
                    )
                }
            }
            else -> {
                val state = _uiState.value
                val wasDenied = state.wasDeniedOnce || canShowRationale != null
                val permanentlyDenied = when {
                    canShowRationale != null -> {
                        permissionManager.isPermanentlyDenied(
                            wasDeniedOnce = true,
                            canShowRationale = canShowRationale
                        )
                    }
                    else -> state.isPermanentlyDenied
                }
                _uiState.update {
                    it.copy(
                        status = if (wasDenied) {
                            MediaPermissionStatus.DENIED
                        } else {
                            MediaPermissionStatus.NOT_REQUESTED
                        },
                        wasDeniedOnce = wasDenied,
                        isPermanentlyDenied = permanentlyDenied,
                        accessibleImageCount = 0,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun loadAccessibleImageCount(): Int {
        return runCatching { refreshImagesUseCase().size }.getOrDefault(0)
    }
}
