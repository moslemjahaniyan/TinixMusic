package com.tinixmusic.tinixmusic2

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.Builder

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null
    private var currentSongUrl: String? = null

    fun getPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        return exoPlayer!!
    }
    fun playSong(context: Context , url: String){
        val player= getPlayer(context)
        if (currentSongUrl == url && player.isPlaying){
            player.pause()
        }else if (currentSongUrl == url && player.isPlaying){
            player.play()
        }else{
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
            currentSongUrl = url
        }
    }

    fun isPlaying(url: String): Boolean{
        return currentSongUrl == url && (exoPlayer?.isPlaying ?: false)
    }

    fun reLease(){
        exoPlayer?.release()
        exoPlayer = null
        currentSongUrl = null
    }
}