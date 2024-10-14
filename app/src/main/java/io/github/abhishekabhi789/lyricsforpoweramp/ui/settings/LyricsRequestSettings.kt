package io.github.abhishekabhi789.lyricsforpoweramp.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import io.github.abhishekabhi789.lyricsforpoweramp.R
import io.github.abhishekabhi789.lyricsforpoweramp.ui.components.PermissionDialog
import io.github.abhishekabhi789.lyricsforpoweramp.utils.AppPreference

@SuppressLint("InlinedApi", "PermissionLaunchedDuringComposition")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LyricsRequestSettings(modifier: Modifier = Modifier) {
    SettingsGroup(
        modifier = modifier,
        title = stringResource(R.string.settings_lyrics_request_label),
        icon = Icons.Default.Lyrics
    ) {
        val context = LocalContext.current
        var hasNotificationPermission by rememberSaveable { mutableStateOf(false) }
        var askPermission by rememberSaveable { mutableStateOf(false) }
        var showPermissionDialog by rememberSaveable { mutableStateOf(false) }
        val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        hasNotificationPermission = when (permissionState.status) {
            PermissionStatus.Granted -> true
            is PermissionStatus.Denied -> false
        }
        LaunchedEffect(askPermission) {
            if (askPermission) {
                if (permissionState.status.shouldShowRationale) {
                    showPermissionDialog = true
                } else {
                    permissionState.launchPermissionRequest()
                }
                askPermission = false // resetting
            }
        }
        if (showPermissionDialog) {
            PermissionDialog(
                onConfirm = {
                    askPermission = false
                    showPermissionDialog = false
                    val intent =
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                    context.startActivity(intent)
                },
                onDismiss = {
                    askPermission = false
                    showPermissionDialog = false
                    Toast.makeText(
                        context,
                        context.getString(R.string.settings_permission_toast_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
        var fallbackToSearch by remember {
            mutableStateOf(AppPreference.getSearchIfGetFailed(context))
        }
        BasicSettings(
            label = stringResource(id = R.string.settings_fallback_to_search_label),
            description = stringResource(id = R.string.settings_fallback_to_search_description),
            modifier = Modifier
        ) {
            Switch(
                checked = fallbackToSearch,
                onCheckedChange = {
                    fallbackToSearch = it
                    AppPreference.setSearchIfGetFailed(context, it)
                },
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        var showNotification by remember {
            mutableStateOf(AppPreference.getShowNotification(context))
        }
        BasicSettings(
            label = stringResource(id = R.string.settings_request_fail_notification_label),
            description = stringResource(id = R.string.settings_request_fail_notification_description),
            modifier = Modifier
        ) {
            Switch(
                checked = showNotification,
                onCheckedChange = {
                    showNotification = it
                    AppPreference.setShowNotification(context, it)
                },
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AnimatedVisibility(
                visible = showNotification && !hasNotificationPermission,
                enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = slideOutVertically() + shrinkVertically() + fadeOut()
            ) {
                BasicSettings(
                    label = stringResource(R.string.settings_notification_permission_label),
                    description = stringResource(R.string.settings_notification_permission_description)
                ) {
                    Button(
                        onClick = { askPermission = true },
                        enabled = !hasNotificationPermission
                    ) {
                        Text(stringResource(R.string.settings_permission_button_grant))
                    }
                }
            }
        }
        var overwriteNotification by remember {
            mutableStateOf(AppPreference.getOverwriteNotification(context))
        }
        AnimatedVisibility(
            visible = showNotification,
            enter = slideInVertically() + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            BasicSettings(
                label = stringResource(id = R.string.settings_overwrite_existing_notification_label),
                description = stringResource(id = R.string.settings_overwrite_existing_notification_description),
                modifier = Modifier.alpha(if (hasNotificationPermission) 1.0f else 0.7f)
            ) {
                Switch(
                    checked = overwriteNotification,
                    enabled = hasNotificationPermission,
                    onCheckedChange = {
                        overwriteNotification = it
                        AppPreference.setOverwriteNotification(context, it)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}