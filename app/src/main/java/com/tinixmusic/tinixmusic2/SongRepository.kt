package com.tinixmusic.tinixmusic2

object SongRepository {
    val songs = listOf(
        Song(id = "1",
        title = "Envy",
        artist = "Slxughter",
        downloadUrl = "https://example.com/envy.mp3",
        imageResId = R.drawable.music_logo
        ),
        Song(
            id = "2",
            title = "Paint It Black",
            artist = "Rolling Stones",
            downloadUrl = "https://example.com/paint.mp3",
            imageResId = R.drawable.music_logo
        ),
        Song(
            id = "3",
            title = "Believer",
            artist = "Imagine Dragons",
            downloadUrl = "https://example.com/believer.mp3",
            imageResId = R.drawable.music_logo
        ),
        Song(
            id = "4",
            title = "Shape of You",
            artist = "Ed Sheeran",
            downloadUrl = "https://example.com/shape.mp3",
            imageResId = R.drawable.music_logo
        ),
        Song(
            id = "5",
            title = "Blinding Lights",
            artist = null,
            downloadUrl = "https://example.com/blinding.mp3",

        )
    )
}