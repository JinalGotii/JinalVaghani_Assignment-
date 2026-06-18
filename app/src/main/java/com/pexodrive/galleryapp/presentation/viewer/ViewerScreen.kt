package com.pexodrive.galleryapp.presentation.viewer

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.pexodrive.galleryapp.R
import com.pexodrive.galleryapp.domain.model.MediaItem
import com.pexodrive.galleryapp.presentation.grid.GridEvent
import com.pexodrive.galleryapp.presentation.grid.GridViewModel
import com.pexodrive.galleryapp.ui.theme.DarkColorScheme
import com.pexodrive.galleryapp.ui.theme.LightColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewerScreen(
    startIndex: Int,
    onBack: () -> Unit,
    gridBackStackEntry: NavBackStackEntry,
    viewModel: GridViewModel = hiltViewModel(gridBackStackEntry)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val images = uiState.images
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var itemToDelete by remember { mutableStateOf<MediaItem?>(null) }

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        viewModel.onDeleteConfirmed(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(uiState.deletingIds, images.size) {
        if (images.isEmpty() && uiState.deletingIds.isEmpty()) {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GridEvent.Share -> context.startActivity(Intent.createChooser(event.intent, null))
                is GridEvent.RequestDeleteConfirmation -> deleteLauncher.launch(event.request)
                is GridEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    BackHandler(onBack = onBack)

    if (images.isEmpty()) {
        return
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_single_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItems(listOf(item))
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = if (isSystemInDarkTheme()) {
                        DarkColorScheme.primary
                    } else {
                        LightColorScheme.primary
                    })
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel),color = if (isSystemInDarkTheme()) {
                        DarkColorScheme.primary
                    } else {
                        LightColorScheme.primary
                    })
                }
            }
        )
    }

    Box {
        ViewerScreenContent(
            images = images,
            startIndex = startIndex,
            deletingIds = uiState.deletingIds,
            onBack = onBack,
            onShare = { item -> viewModel.shareItems(listOf(item)) },
            onDelete = { item -> itemToDelete = item }
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
