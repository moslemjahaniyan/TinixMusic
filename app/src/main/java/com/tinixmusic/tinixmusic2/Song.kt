package com.tinixmusic.tinixmusic2

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val downloadUrl: String,
    val imageResId: Int? = null
)
