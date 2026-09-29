package com.tinixmusic.tinixmusic2

import android.app.PendingIntent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        // ۱. ساخت ExoPlayer با تنظیمات مناسب برای موزیک
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true // خودکار مدیریت کن وقتی هدفون جدا شد، صدا رو قطع کن
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        // ۲. یه PendingIntent بساز که وقتی روی Notification کلیک شد، اپ باز بشه
        val sessionActivityPendingIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.let {
                PendingIntent.getActivity(
                    this,
                    0,
                    it,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

        // ۳. MediaSession بساز و ExoPlayer رو بهش وصل کن
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent!!) // علامت !! جدا اضافه شد
            .build()
    }

    // این تابع به MediaController می‌گه که MediaSession کدومه
    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}