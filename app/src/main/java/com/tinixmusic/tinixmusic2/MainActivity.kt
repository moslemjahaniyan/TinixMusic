package com.tinixmusic.tinixmusic2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val songs = listOf(Song("Envy" , "Slxughter" , "https://example.com/envy.mp3"),

            Song("Paint It Black", "Rolling Stones", "https://example.com/paint.mp3"),
            Song("Believer", "Imagine Dragons", "https://example.com/believer.mp3"),
            Song("Shape of You", "Ed Sheeran", "https://example.com/shape.mp3"),
            Song("Blinding Lights", "The Weeknd", "https://example.com/blinding.mp3")
        )

        setContent {
            MaterialTheme{
                SongList(songs)
            }
        }
    }



    fun createWelcomeMessage(appName: String): String{
        return "به $appName خوش آمدید"
    }
    fun createAppInfo(name: String, version: Int): String{
        return "$name نسخه  $version"
    }

}

@Composable
fun SongList(song: List<Song>){
    LazyColumn{
        items(song){song ->
            SongItem(song)
        }
    }
}

@Composable
fun SongItem(song: Song){
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(

            text = when(song.artist){
                "Ed Sheeran" ->  song.artist + (" محبوب")
                else -> song.artist
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

