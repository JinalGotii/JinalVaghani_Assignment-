package com.pexodrive.galleryapp.presentation.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pexodrive.galleryapp.R
import com.pexodrive.galleryapp.ui.theme.PermissionAccentYellow
import com.pexodrive.galleryapp.ui.theme.PermissionCardBackground
import com.pexodrive.galleryapp.ui.theme.PermissionTextSecondary

@Composable
fun PartialAccessInfoCard(
    imageCount: Int,
    onGrantFullAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(PermissionCardBackground, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.permission_partial_title),
            color = PermissionAccentYellow,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (imageCount > 0) {
                stringResource(R.string.permission_partial_granted, imageCount)
            } else {
                stringResource(R.string.permission_partial_message)
            },
            color = PermissionTextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onGrantFullAccess,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.permission_grant_more),
                color = PermissionAccentYellow
            )
        }
    }
}
@Composable
fun DeniedRationaleCard(
    isPermanentlyDenied: Boolean,
    onGoToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = stringResource(R.string.permission_denied_title),
                color = PermissionAccentYellow,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPermanentlyDenied) {
                    stringResource(R.string.permission_denied_permanent_message)
                } else {
                    stringResource(R.string.permission_denied_rationale)
                },
                color = PermissionTextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.go_to_settings),
                    color = PermissionAccentYellow,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

}
