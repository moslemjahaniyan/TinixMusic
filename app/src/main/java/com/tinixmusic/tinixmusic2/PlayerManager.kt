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



    fun playPlaylist(
        context: Context,
        songs: List<Song>,
        startIndex: Int = 0
    ){
        connect(context)
        val controller = mediaController ?: return
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.downloadUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(song.imageUrl?.toUri())
                        .build()
                )
                .build()
        }
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()    }

    fun skipNext(){
        mediaController?.seekToNextMediaItem()
    }

    fun skipPrevious(){
        mediaController?.seekToPreviousMediaItem()
    }

    fun hasNext(): Boolean = mediaController?.hasNextMediaItem() ?: false
    fun hasPrevious(): Boolean = mediaController?.hasPreviousMediaItem() ?: false





    // در PlayerManager.kt:
    fun getCurrentPosition(): Long = mediaController?.currentPosition ?: 0L
    fun getDuration(): Long = mediaController?.duration ?: 0L




    fun release() {
        mediaController?.release()
        mediaController = null
    }
}