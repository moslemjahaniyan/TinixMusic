package com.tinixmusic.tinixmusic2

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlayerManager {
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    fun connect(context: Context) {
        if (mediaController != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            mediaController = future.get()
            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentUrl.value = mediaItem?.localConfiguration?.uri?.toString()
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }

    fun togglePlayPause(
        context: Context,
        url: String,
        title: String,
        artist: String?,
        imageUrl: String?
    ) {
        connect(context)
        val controller = mediaController ?: return

        val currentUri = controller.currentMediaItem?.localConfiguration?.uri?.toString()
        if (currentUri == url) {
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        } else {
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setArtworkUri(imageUrl?.toUri())
                        .build()
                )
                .build()
            controller.setMediaItem(mediaItem)
            controller.prepare()
            controller.play()
        }
    }

    fun release() {
        mediaController?.release()
        mediaController = null
    }
}