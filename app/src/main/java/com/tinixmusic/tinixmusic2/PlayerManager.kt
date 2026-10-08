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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



data class NowPlaying(
    val songId: String,
    val title: String,
    val artist: String?,
    val imageUrl: String?,
    val url: String
)



object PlayerManager {
    private var mediaController: MediaController? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentUrl = MutableStateFlow<String?>(null)
    private val _nowPlaying = MutableStateFlow<NowPlaying?>(null)
    val nowPlaying: StateFlow<NowPlaying?> = _nowPlaying.asStateFlow()
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
                    val url = mediaItem?.localConfiguration?.uri?.toString()
                    _currentUrl.value = url

                    if (mediaItem != null && url != null) {
                        _nowPlaying.value = NowPlaying(
                            songId = mediaItem.mediaId,
                            title = mediaItem.mediaMetadata.title?.toString() ?: "",
                            artist = mediaItem.mediaMetadata.artist?.toString(),
                            imageUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
                            url = url
                        )
                    }
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }

    fun togglePlayPause(
        context: Context,
        songId: String,
        url: String,
        title: String,
        artist: String?,
        imageUrl: String?,
        localPath: String? = null
    ) {
        connect(context)
        val controller = mediaController ?: return

        val playUrl = if (localPath != null && java.io.File(localPath).exists()) {
            "file://$localPath"
        } else {
            url
        }

        val currentUri = controller.currentMediaItem?.localConfiguration?.uri?.toString()
        if (currentUri == playUrl) {
            if (controller.isPlaying) controller.pause() else controller.play()
        } else {
            val mediaItem = MediaItem.Builder()
                .setUri(playUrl)
                .setMediaId(songId) // ✅ اضافه شد
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
            MediaItem.Builder().setMediaId(song.id)
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




    private var sleepTimerJob: Job? = null
    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null)
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalMs = minutes * 60_000L
        sleepTimerJob = CoroutineScope(Dispatchers.Default).launch {
            var remaining = totalMs
            while (remaining > 0) {
                _sleepTimerRemaining.value = remaining
                delay(1000)
                remaining -= 1000
            }
            // تایمر تموم شد → Pause کن
            mediaController?.pause()
            _sleepTimerRemaining.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemaining.value = null
    }



    fun release() {
        mediaController?.release()
        mediaController = null
        _nowPlaying.value = null
    }







}