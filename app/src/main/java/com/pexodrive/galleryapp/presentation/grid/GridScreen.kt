package com.pexodrive.galleryapp.presentation.grid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pexodrive.galleryapp.R
import com.pexodrive.galleryapp.presentation.common.DeleteAnimatedContent
import com.pexodrive.galleryapp.presentation.common.MediaGridItem
import com.pexodrive.galleryapp.utils.Constants
import coil.imageLoader
import coil.request.ImageRequest
import com.pexodrive.galleryapp.presentation.common.PartialAccessBanner
import com.pexodrive.galleryapp.presentation.navigation.PermissionEntryPoint
import com.pexodrive.galleryapp.ui.theme.DarkColorScheme
import com.pexodrive.galleryapp.ui.theme.LightColorScheme
import com.pexodrive.galleryapp.ui.theme.galleryTopAppBarColors
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GridScreen(
    onNavigateToViewer: (Int) -> Unit,
    viewModel: GridViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    val permissionManager = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PermissionEntryPoint::class.java
    ).permissionManager()

    val fullAccessLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh()
    }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeleteConfirmed(result.resultCode == android.app.Activity.RESULT_OK)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GridEvent.Share -> {
                    context.startActivity(android.content.Intent.createChooser(event.intent, null))
                }
                is GridEvent.RequestDeleteConfirmation -> {
                    deleteLauncher.launch(event.request)
                }
                is GridEvent.ShowSnackbar -> {
                    val message = when (event.message) {
                        "delete_pending" -> context.getString(R.string.delete_pending)
                        "delete_failed" -> context.getString(R.string.delete_failed)
                        else -> event.message
                    }
                    val actionLabel = when (event.actionLabel) {
                        "undo" -> context.getString(R.string.undo)
                        else -> event.actionLabel
                    }
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    when {
                        result == SnackbarResult.ActionPerformed -> viewModel.undoPendingDelete()
                        event.actionLabel == "undo" -> viewModel.confirmPendingDelete()
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.delete_confirm_message,
                        uiState.selectedIds.size
                    ),color = if (isSystemInDarkTheme()) {
                        DarkColorScheme.primary
                    } else {
                        LightColorScheme.primary
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.requestDeleteSelected()
                    }
                ) {
                    Text(stringResource(R.string.delete),color = if (isSystemInDarkTheme()) {
                        DarkColorScheme.primary
                    } else {
                        LightColorScheme.primary
                    })
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    GridScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onToggleColumns = viewModel::toggleColumnCount,
        onClearSelection = viewModel::clearSelection,
        onShareSelected = viewModel::shareSelected,
        onShowDeleteDialog = { showDeleteDialog = true },
        onRefresh = viewModel::refresh,
        onImageClick = { item ->
            context.imageLoader.enqueue(
                ImageRequest.Builder(context)
                    .data(item.uri)
                    .build()
            )
            viewModel.onImageClick(item) { index -> onNavigateToViewer(index) }
        },
        onImageLongClick = viewModel::onImageLongPress,
        onGrantFullAccess = {
            fullAccessLauncher.launch(permissionManager.getFullAccessPermissions())
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GridScreenContent(
    uiState: GridUiState,
    snackbarHostState: SnackbarHostState,
    onToggleColumns: () -> Unit,
    onClearSelection: () -> Unit,
    onShareSelected: () -> Unit,
    onShowDeleteDialog: () -> Unit,
    onRefresh: () -> Unit,
    onImageClick: (com.pexodrive.galleryapp.domain.model.MediaItem) -> Unit,
    onImageLongClick: (com.pexodrive.galleryapp.domain.model.MediaItem) -> Unit,
    onGrantFullAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val hasSelection = uiState.selectedIds.isNotEmpty()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.isSelectionMode) {
                            stringResource(R.string.selected_count, uiState.selectedIds.size)
                        } else {
                            stringResource(R.string.gallery_title)
                        }
                    )
                },
                navigationIcon = {
                    when {
                        uiState.isSelectionMode -> {
                            IconButton(onClick = onClearSelection) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear_selection)
                                )
                            }
                        }
                        else -> {
                            IconButton(onClick = {  }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = stringResource(R.string.menu)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(
                            onClick = onShareSelected,
                            enabled = hasSelection
                        ) {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.share))
                        }
                        IconButton(
                            onClick = onShowDeleteDialog,
                            enabled = hasSelection
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    } else {

                        val iconRes: Int = if ( uiState.columnCount == Constants.GRID_COLUMNS_MAX) {
                            R.drawable.ic_grid_3_columns
                        } else {
                            R.drawable.ic_grid_2_columns
                        }
                        val targetColumns = if (uiState.columnCount == Constants.GRID_COLUMNS_MAX) {
                            Constants.GRID_COLUMNS_MIN
                        } else {
                            Constants.GRID_COLUMNS_MAX
                        }

                        IconButton(onClick = onToggleColumns, modifier = modifier) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = stringResource(R.string.toggle_columns, targetColumns)
                            )
                        }
                    }
                },
                colors = galleryTopAppBarColors(),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isSelectionMode) {
                GridSelectionBottomBar(
                    onShare = onShareSelected,
                    onDelete = onShowDeleteDialog,
                    enabled = hasSelection
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.images.isEmpty() -> {
                    GridEmptyState(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.showPartialAccessBanner) {
                            PartialAccessBanner(
                                imageCount = uiState.images.size,
                                onGrantFullAccess = onGrantFullAccess
                            )
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(uiState.columnCount),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.images,
                                key = { it.id }
                            ) { item ->
                                DeleteAnimatedContent(
                                    isDeleting = item.id in uiState.deletingIds,
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = tween(Constants.DELETE_ANIMATION_DURATION_MS),
                                        fadeOutSpec = tween(Constants.DELETE_ANIMATION_DURATION_MS),
                                        placementSpec = tween(Constants.DELETE_ANIMATION_DURATION_MS)
                                    )
                                ) {
                                    MediaGridItem(
                                        item = item,
                                        isSelected = item.id in uiState.selectedIds,
                                        onClick = { onImageClick(item) },
                                        onLongClick = { onImageLongClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Empty View
@Composable
fun GridEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.empty_gallery_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.empty_gallery_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
