package com.tinixmusic.tinixmusic2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            MaterialTheme{
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "songList"){
                    composable("songList"){
                        SongListScreen(navController = navController)
                    }
                    composable("songDetail/{songId}"){backStackEntry ->
                        val songId = backStackEntry.arguments?.getString("songId")
                        SongDetailScreen(songId = songId , navController = navController)

                    }
                }
            }
        }
    }


}

@Composable
fun SongList(songs: List<Song>){
    LazyColumn{
        items(songs){song ->
            SongItem(song)
        }
    }
}

@Composable
fun SongItem(song: Song){
    val context = LocalContext.current //for show Toast

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            Toast
                .makeText(context, "${song.title} کلیک شد", Toast.LENGTH_SHORT)
                .show()

        }
        .padding(16.dp)
        , verticalAlignment = Alignment.CenterVertically) {
        Image(painter = painterResource(id = song.imageResId ?: R.drawable.music_no_logo), contentDescription = "تصویر آهنگ ${song.title}"
        ,modifier = Modifier.size(64.dp))
        
    }

    Spacer(modifier = Modifier.width(16.dp))

    Column() {
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(

            text = song.artist ?:  "خواننده نامشخص",  // اگه null بود، متن جایگزین
            style = MaterialTheme.typography.bodyMedium,
            color = if (song.artist == null) Color.Red else Color.Unspecified
        )
    }
}

