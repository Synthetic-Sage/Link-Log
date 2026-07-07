package com.synthetic.linklog.ui.player

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerScreen(
    linkId: Long,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val videoData by viewModel.videoData.collectAsState()

    // Initialize ExoPlayer
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    // Load data
    LaunchedEffect(linkId) {
        viewModel.loadVideo(linkId)
    }

    // Set media item when data is available
    LaunchedEffect(videoData) {
        videoData?.localUri?.let { uriString ->
            val mediaItem = MediaItem.fromUri(Uri.parse(uriString))
            exoPlayer.setMediaItem(mediaItem)
            
            // Resume from last position
            if ((videoData?.playbackPositionMs ?: 0L) > 0L) {
                exoPlayer.seekTo(videoData!!.playbackPositionMs)
            }
            
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Manage lifecycle and save state
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                    viewModel.savePlaybackPosition(linkId, exoPlayer.currentPosition)
                }
                Lifecycle.Event.ON_DESTROY -> {
                    viewModel.savePlaybackPosition(linkId, exoPlayer.currentPosition)
                    exoPlayer.release()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            viewModel.savePlaybackPosition(linkId, exoPlayer.currentPosition)
            exoPlayer.release()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // UI
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true
                keepScreenOn = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
