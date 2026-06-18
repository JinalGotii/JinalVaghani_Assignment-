package com.pexodrive.galleryapp.presentation.permission

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pexodrive.galleryapp.R
import com.pexodrive.galleryapp.ui.theme.PermissionAccentYellow
import com.pexodrive.galleryapp.ui.theme.PermissionBackground
import com.pexodrive.galleryapp.ui.theme.PermissionButtonText
import com.pexodrive.galleryapp.ui.theme.PermissionIconContainer
import com.pexodrive.galleryapp.ui.theme.PermissionTextPrimary
import com.pexodrive.galleryapp.ui.theme.PermissionTextSecondary
import com.pexodrive.galleryapp.ui.theme.PrimaryBlue

@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as ComponentActivity

    fun canShowRationale(): Boolean = viewModel.getFullAccessPermissions().any { permission ->
        ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onPermissionResult(canShowRationale())
    }

    val requestFullAccess: () -> Unit = {
        permissionLauncher.launch(viewModel.getFullAccessPermissions())
    }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshStatus()
        onPauseOrDispose { }
    }

    val onRequestPermission: () -> Unit = {
        viewModel.onRequestPermission(
            canShowRationale = canShowRationale(),
            requestPermission = requestFullAccess
        )
    }

    PermissionScreenContent(
        uiState = uiState,
        onPermissionGranted = onPermissionGranted,
        onRequestPermission = onRequestPermission,
        onGrantFullAccess = requestFullAccess,
        onGoToSettings = { context.startActivity(viewModel.createSettingsIntent()) }
    )
}

@Composable
fun PermissionScreenContent(
    uiState: PermissionUiState,
    onPermissionGranted: () -> Unit,
    onRequestPermission: () -> Unit,
    onGrantFullAccess: () -> Unit,
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()

            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = PermissionAccentYellow
            )
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(R.drawable.appicon3),
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.gallery_title),
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.permission_photo_description),
                color = PermissionTextSecondary,
                fontSize = 14.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))


            OutlinedButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PermissionIconContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_permission_gallery),
                            contentDescription = null,
                            tint = PermissionAccentYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.permission_photo_title),
                            color = PermissionAccentYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.permission_hero_description),
                            color = PermissionTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

            }

            if (uiState.showNotRequestedHint) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.grant_permission),
                        color = PermissionTextSecondary

                    )
                }
            }

            if (uiState.showPartialAccessInfo) {
                Spacer(modifier = Modifier.height(16.dp))
                PartialAccessInfoCard(
                    imageCount = uiState.accessibleImageCount,
                    onGrantFullAccess = onGrantFullAccess
                )
            }

            if (uiState.showDeniedRationale) {
                Spacer(modifier = Modifier.height(16.dp))
                DeniedRationaleCard(
                    isPermanentlyDenied = uiState.isPermanentlyDenied,
                    onGoToSettings = onGoToSettings
                )
            }
        }

        if (uiState.canContinue) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPermissionGranted,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PermissionAccentYellow,
                        contentColor = PermissionButtonText
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (uiState.showPartialAccessInfo) {
                            stringResource(R.string.continue_with_selected)
                        } else {
                            stringResource(R.string.permission_continue)
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }
    }
}
