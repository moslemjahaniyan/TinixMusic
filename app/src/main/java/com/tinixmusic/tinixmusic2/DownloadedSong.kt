package com.tinixmusic.tinixmusic2


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSong(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String?,
    val localPath: String,        // مسیر فایل روی گوشی
    val imageUrl: String?,
    val downloadedAt: Long = System.currentTimeMillis()
)