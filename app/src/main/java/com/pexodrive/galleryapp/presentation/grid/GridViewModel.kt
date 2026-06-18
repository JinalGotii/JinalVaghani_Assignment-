package com.pexodrive.galleryapp.presentation.grid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pexodrive.galleryapp.domain.model.MediaItem
import com.pexodrive.galleryapp.domain.usecase.DeleteImagesUseCase
import com.pexodrive.galleryapp.domain.usecase.GetGridColumnsUseCase
import com.pexodrive.galleryapp.domain.usecase.GetImagesUseCase
import com.pexodrive.galleryapp.domain.usecase.RefreshImagesUseCase
import com.pexodrive.galleryapp.domain.usecase.SetGridColumnsUseCase
import com.pexodrive.galleryapp.utils.Constants
import com.pexodrive.galleryapp.utils.DeleteHelper
import com.pexodrive.galleryapp.utils.PermissionManager
import com.pexodrive.galleryapp.utils.ShareUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GridViewModel @Inject constructor(
    private val getImagesUseCase: GetImagesUseCase,
    private val refreshImagesUseCase: RefreshImagesUseCase,
    private val deleteImagesUseCase: DeleteImagesUseCase,
    private val getGridColumnsUseCase: GetGridColumnsUseCase,
    private val setGridColumnsUseCase: SetGridColumnsUseCase,
    private val shareUtils: ShareUtils,
    private val deleteHelper: DeleteHelper,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GridUiState(images = getImagesUseCase.peek()))
    val uiState: StateFlow<GridUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GridEvent>()
    val events: SharedFlow<GridEvent> = _events.asSharedFlow()

    private var allImages: List<MediaItem> = emptyList()

    init {
        viewModelScope.launch {
            combine(
                getImagesUseCase(),
                getGridColumnsUseCase()
            ) { images, columns ->
                images to columns
            }.collect { (images, columns) ->
                allImages = images
                _uiState.update { state ->
                    state.copy(
                        images = images,
                        columnCount = columns,
                        isRefreshing = false,
                        showPartialAccessBanner = permissionManager.hasPartialMediaAccess(),
                        selectedIds = state.selectedIds.intersect(images.map { it.id }.toSet()),
                        deletingIds = state.deletingIds.intersect(images.map { it.id }.toSet())
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            refreshImagesUseCase()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun setColumnCount(columns: Int) {
        viewModelScope.launch {
            setGridColumnsUseCase(columns)
        }
    }

    fun toggleColumnCount() {
        viewModelScope.launch {
            val newCount = if (_uiState.value.columnCount == Constants.GRID_COLUMNS_MIN) {
                Constants.GRID_COLUMNS_MAX
            } else {
                Constants.GRID_COLUMNS_MIN
            }
            setGridColumnsUseCase(newCount)
        }
    }

    fun onImageClick(item: MediaItem, onOpenViewer: (Int) -> Unit) {
        if (_uiState.value.isSelectionMode) {
            toggleSelection(item.id)
        } else {
            val index = _uiState.value.images.indexOfFirst { it.id == item.id }
            if (index >= 0) onOpenViewer(index)
        }
    }

    fun onImageLongPress(item: MediaItem) {
        if (!_uiState.value.isSelectionMode) {
            _uiState.update {
                it.copy(isSelectionMode = true, selectedIds = setOf(item.id))
            }
        }
    }

    fun toggleSelection(id: Long) {
        _uiState.update { state ->
            val newSelection = state.selectedIds.toMutableSet()
            if (id in newSelection) newSelection.remove(id) else newSelection.add(id)
            state.copy(
                selectedIds = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(isSelectionMode = false, selectedIds = emptySet()) }
    }

    fun shareSelected() {
        val selected = getSelectedItems()
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _events.emit(GridEvent.Share(shareUtils.createShareIntent(selected)))
        }
    }

    fun requestDeleteSelected() {
        val selected = getSelectedItems()
        if (selected.isEmpty()) return

        if (deleteHelper.requiresSystemConfirmation()) {
            val request = deleteHelper.createDeleteRequest(selected)
            if (request != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(pendingDeleteItems = selected) }
                    _events.emit(GridEvent.RequestDeleteConfirmation(request))
                }
            }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(pendingDeleteItems = selected) }
                _events.emit(
                    GridEvent.ShowSnackbar(
                        message = "delete_pending",
                        actionLabel = "undo"
                    )
                )
            }
        }
    }

    fun onDeleteConfirmed(success: Boolean) {
        val pending = _uiState.value.pendingDeleteItems ?: return
        if (success) {
            viewModelScope.launch {
                playDeleteAnimation(pending.map { it.id }.toSet())
                _uiState.update { it.copy(pendingDeleteItems = null) }
                clearSelection()
                _events.emit(GridEvent.ShowSnackbar("${pending.size} deleted"))
            }
        } else {
            _uiState.update { it.copy(pendingDeleteItems = null, deletingIds = emptySet()) }
        }
    }

    fun confirmPendingDelete() {
        val pending = _uiState.value.pendingDeleteItems ?: return
        viewModelScope.launch {
            playDeleteAnimation(pending.map { it.id }.toSet())
            val result = deleteImagesUseCase(pending)
            _uiState.update { it.copy(pendingDeleteItems = null) }
            clearSelection()
            if (result.isSuccess) {
                _events.emit(GridEvent.ShowSnackbar("${pending.size} deleted"))
            } else {
                _events.emit(GridEvent.ShowSnackbar("delete_failed"))
            }
        }
    }

    fun undoPendingDelete() {
        _uiState.update { it.copy(pendingDeleteItems = null, deletingIds = emptySet()) }
    }

    fun shareItems(items: List<MediaItem>) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            _events.emit(GridEvent.Share(shareUtils.createShareIntent(items)))
        }
    }

    fun deleteItems(items: List<MediaItem>) {
        if (items.isEmpty()) return
        if (deleteHelper.requiresSystemConfirmation()) {
            val request = deleteHelper.createDeleteRequest(items)
            if (request != null) {
                viewModelScope.launch {
                    _uiState.update { it.copy(pendingDeleteItems = items) }
                    _events.emit(GridEvent.RequestDeleteConfirmation(request))
                }
            }
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(pendingDeleteItems = items) }
                confirmPendingDelete()
            }
        }
    }


    private fun getSelectedItems(): List<MediaItem> {
        val ids = _uiState.value.selectedIds
        return allImages.filter { it.id in ids }
    }

    private suspend fun playDeleteAnimation(itemIds: Set<Long>) {
        if (itemIds.isEmpty()) return
        _uiState.update { it.copy(deletingIds = it.deletingIds + itemIds) }
        delay(Constants.DELETE_ANIMATION_DURATION_MS.toLong())
        _uiState.update { it.copy(deletingIds = it.deletingIds - itemIds) }
    }
}
