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


enum class RepeatMode(val label: String) {
    OFF("بدون تکرار"),
    REPEAT_ALL("تکرار همه"),
    REPEAT_ONE("تکرار یکی")
}

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

    private val _currentSongId = MutableStateFlow<String?>(null)
    val currentSongId: StateFlow<String?> = _currentSongId.asStateFlow()



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
                    syncNowPlaying(mediaItem)
                }

                // ✅ این‌ها هم اضافه شدن تا هیچ تغییری از دست نره
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    syncNowPlaying(mediaController?.currentMediaItem)
                }

                override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                    syncNowPlaying(mediaController?.currentMediaItem)
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }

    /** خواندن اطلاعات آهنگ فعلی از روی MediaItem و به‌روزرسانی Stateها */
    private fun syncNowPlaying(mediaItem: MediaItem?) {
        if (mediaItem == null) return

        val url = mediaItem.localConfiguration?.uri?.toString()

        _currentUrl.value = url
        _currentSongId.value = mediaItem.mediaId

        // ✅ مهم: مستقل از url هم NowPlaying رو آپدیت کن
        _nowPlaying.value = NowPlaying(
            songId = mediaItem.mediaId,
            title = mediaItem.mediaMetadata.title?.toString() ?: "",
            artist = mediaItem.mediaMetadata.artist?.toString(),
            imageUrl = mediaItem.mediaMetadata.artworkUri?.toString(),
            url = url ?: ""
        )
    }

    fun togglePlayPause(
        context: Context,
        songId: String,
        url: String,
        title: String,
        artist: String?,
        imageUrl: String?,
        localPath: String? = null,
        playlist: List<Song>? = null       // ✅ جدید
    ) {
        connect(context)
        val controller = mediaController ?: return

        val playUrl = when {
            localPath.isNullOrBlank() -> url
            localPath.startsWith("content://") || localPath.startsWith("file://") -> localPath
            java.io.File(localPath).exists() -> "file://$localPath"
            else -> url
        }

        val currentMediaId = controller.currentMediaItem?.mediaId

        // ۱. همین آهنگ در حال پخشه → فقط toggle کن
        if (currentMediaId == songId) {
            if (controller.isPlaying) controller.pause() else controller.play()
            return
        }

        // ۲. اگه playlist داده شده و آهنگ توش هست → کل صف رو ست کن
        if (playlist != null) {
            val index = playlist.indexOfFirst { it.id == songId }
            if (index >= 0) {
                val mediaItems = playlist.map { song ->
                    // برای آهنگ هدف از playUrl (که ممکنه local باشه) استفاده کن
                    val uri = if (song.id == songId) playUrl else song.downloadUrl
                    MediaItem.Builder()
                        .setMediaId(song.id)
                        .setUri(uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setArtworkUri(song.imageUrl?.toUri())
                                .build()
                        )
                        .build()
                }
                controller.setMediaItems(mediaItems, index, 0L)
                controller.prepare()
                controller.play()
                return
            }
        }

        // ۳. fallback: تک‌آهنگ
        val mediaItem = MediaItem.Builder()
            .setUri(playUrl)
            .setMediaId(songId)
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

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
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





    // State برای Shuffle
    private val _isShuffleOn = MutableStateFlow(false)
    val isShuffleOn: StateFlow<Boolean> = _isShuffleOn.asStateFlow()

    // State برای Repeat
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newValue = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newValue
        _isShuffleOn.value = newValue
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        val next = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
        }
        _repeatMode.value = next
        controller.repeatMode = when (next) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
        }
    }


}