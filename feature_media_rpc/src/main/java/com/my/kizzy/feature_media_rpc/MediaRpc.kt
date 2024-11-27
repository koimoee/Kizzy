/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * MediaRpc.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_media_rpc

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.my.kizzy.feature_rpc_base.AppUtils
import com.my.kizzy.feature_rpc_base.services.AppDetectionService
import com.my.kizzy.feature_rpc_base.services.CustomRpcService
import com.my.kizzy.feature_rpc_base.services.ExperimentalRpc
import com.my.kizzy.feature_rpc_base.services.MediaRpcService
import com.my.kizzy.preference.Prefs
import com.my.kizzy.preference.Prefs.MEDIA_RPC_APP_ICON
import com.my.kizzy.preference.Prefs.MEDIA_RPC_ARTIST_NAME
import com.my.kizzy.preference.Prefs.MEDIA_RPC_ALBUM_NAME
import com.my.kizzy.preference.Prefs.MEDIA_RPC_ENABLE_TIMESTAMPS
import com.my.kizzy.preference.Prefs.MEDIA_RPC_HIDE_ON_PAUSE
import com.my.kizzy.preference.Prefs.MEDIA_RPC_SHOW_PLAYBACK_STATE
import com.my.kizzy.preference.Prefs.MEDIA_RPC_SHOW_ALBUM_ART
import com.my.kizzy.preference.Prefs.SWAP
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.BackButton
import com.my.kizzy.ui.components.SwitchBar
import com.my.kizzy.ui.components.dialog.SingleChoiceItem
import com.my.kizzy.ui.components.preference.PreferenceSwitch
import com.my.kizzy.ui.components.preference.PreferencesHint
import androidx.compose.material3.AlertDialog
import com.my.kizzy.ui.components.SettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaRPC(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    var mediaRpcRunning by remember { mutableStateOf(AppUtils.mediaRpcRunning()) }
    var isArtistEnabled by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_ARTIST_NAME, false]) }
    var isAlbumEnabled by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_ALBUM_NAME, false]) }
    var isAppIconEnabled by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_APP_ICON, false]) }
    var isTimestampsEnabled by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_ENABLE_TIMESTAMPS, false]) }
    var hideOnPause by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_HIDE_ON_PAUSE, false]) }
    var ShowAlbumArt by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_SHOW_ALBUM_ART, false]) }
    var isShowPlaybackState by remember { mutableStateOf(Prefs[Prefs.MEDIA_RPC_SHOW_PLAYBACK_STATE, false]) }
    var hasNotificationAccess by remember { mutableStateOf(context.hasNotificationAccess()) }
    var swapConfig by remember {
        mutableStateOf(Prefs[Prefs.SWAP, "appname"])
    }
    var showSwapDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.main_mediaRpc),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = { BackButton { onBackPressed() } }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            AnimatedVisibility(visible = !hasNotificationAccess) {
                PreferencesHint(
                    title = stringResource(id = R.string.permission_required),
                    description = stringResource(id = R.string.request_for_notification_access),
                    icon = Icons.Default.Warning,
                ) {
                    when (context.hasNotificationAccess()) {
                        true -> hasNotificationAccess = true
                        false -> context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                }
            }
            SwitchBar(
                title = stringResource(id = R.string.enable_mediaRpc),
                isChecked = mediaRpcRunning,
                enabled = hasNotificationAccess
            ) {
                mediaRpcRunning = !mediaRpcRunning
                when (mediaRpcRunning) {
                    true -> {
                        context.stopService(Intent(context, AppDetectionService::class.java))
                        context.stopService(Intent(context, CustomRpcService::class.java))
                        context.stopService(Intent(context, ExperimentalRpc::class.java))
                        context.startService(Intent(context, MediaRpcService::class.java))
                    }
                    false -> context.stopService(Intent(context, MediaRpcService::class.java))
                }
            }
            LazyColumn {
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.enable_artist_name),
                        icon = Icons.Default.Audiotrack,
                        isChecked = isArtistEnabled,
                    ) {
                        isArtistEnabled = !isArtistEnabled
                        Prefs[Prefs.MEDIA_RPC_ARTIST_NAME] = isArtistEnabled
                    }
                }

                item {
                SettingItem(
                    title = stringResource(id = R.string.swap_name),
                    description = stringResource(id = R.string.swap_name_desc),
                    icon = Icons.Default.Sync
                ) {
                    showSwapDialog = true
                   }
               }
                
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.enable_album_name),
                        icon = Icons.Default.Album,
                        isChecked = isAlbumEnabled
                    ) {
                        isAlbumEnabled = !isAlbumEnabled
                        Prefs[Prefs.MEDIA_RPC_ALBUM_NAME] = isAlbumEnabled
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.show_app_icon),
                        icon = Icons.Default.Apps,
                        isChecked = isAppIconEnabled,
                    ) {
                        isAppIconEnabled = !isAppIconEnabled
                        Prefs[Prefs.MEDIA_RPC_APP_ICON] = isAppIconEnabled
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.enable_timestamps),
                        icon = Icons.Default.Timer,
                        isChecked = isTimestampsEnabled,
                    ) {
                        isTimestampsEnabled = !isTimestampsEnabled
                        Prefs[Prefs.MEDIA_RPC_ENABLE_TIMESTAMPS] = isTimestampsEnabled
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.show_playback_state),
                        icon = Icons.Default.PlayCircle,
                        isChecked = isShowPlaybackState,
                    ) {
                        isShowPlaybackState = !isShowPlaybackState
                        Prefs[Prefs.MEDIA_RPC_SHOW_PLAYBACK_STATE] = isShowPlaybackState
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.hide_on_pause),
                        icon = Icons.Default.PauseCircle,
                        isChecked = hideOnPause,
                    ) {
                        hideOnPause = !hideOnPause
                        Prefs[Prefs.MEDIA_RPC_HIDE_ON_PAUSE] = hideOnPause
                    }
                }
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.show_album_art),
                        icon = Icons.Default.AutoAwesome,
                        isChecked = ShowAlbumArt,
                    ) {
                        ShowAlbumArt = !ShowAlbumArt
                        Prefs[Prefs.MEDIA_RPC_SHOW_ALBUM_ART] = ShowAlbumArt
                    }
                }
            }
            if (showSwapDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSwapDialog = false
                },
                confirmButton = {},
                text = {
                    val statusMap = mapOf(
                        stringResource(R.string.swap_appname) to "appname",
                        stringResource(R.string.swap_songname) to "songname",
                        stringResource(R.string.swap_artistname) to "artistname",
                        stringResource(R.string.swap_albumname) to "albumname",
                    )
                    Column {
                        statusMap.forEach { (key, value) ->
                            SingleChoiceItem(
                                text = key,
                                selected = value == swapConfig
                            ) {
                                swapConfig = value
                                Prefs[Prefs.SWAP] = value
                                showSwapDialog = false
                            }
                        }
                    }
                }
            )
          }
        }
    }
}
