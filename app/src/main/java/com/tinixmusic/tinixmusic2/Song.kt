package com.tinixmusic.tinixmusic2

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val downloadUrl: String,
    val imageUrl: String? = null,
    val versions: List<SongVersion> = emptyList()
)

data class SongVersion(
    val labelEn: String,  // مثلاً "Slowed"
    val labelFa: String,  // مثلاً "آهسته"
    val url: String
)