package com.tinixmusic.tinixmusic2



import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import kotlinx.coroutines.delay

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Pause
@Composable
fun SongDetailScreen(songId: String?, navController: NavController) {
    var song by remember { mutableStateOf<Song?>(null) }
    var isLoading by remember { mutableStateOf(true)    }
    val context = LocalContext.current


    LaunchedEffect(songId){
        try {
            val allSongs = SongRepository.getSong()
            song = allSongs.find { it.id == songId }
        } catch (e: Exception){
            // خطا رو نادیده می‌گیریم یا نمایش می‌دیم
        }finally {
            isLoading = false
        }
    }

    if (isLoading){
        Box(modifier = Modifier.fillMaxSize() , contentAlignment = Alignment.Center){
            CircularProgressIndicator()
        }
        return
    }

    val currentSong = song
    if (currentSong == null){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text("آهنگ پیدا نشد")
        }
        return
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {
        // دکمه‌ی بازگشت
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "بازگشت"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // تصویر آهنگ
        AsyncImage(
            model = currentSong.imageUrl ?: R.drawable.music_logo,
            contentDescription = "تصویر ${currentSong.title}",
            modifier = Modifier.size(128.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // اطلاعات
        // به جای این:
        /*
        Text(
            text = currentSong.title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = currentSong.artist ?: "خواننده نامشخص",
            style = MaterialTheme.typography.bodyLarge
        )
*/

        // این رو بذار:
        val context = LocalContext.current
        val favorites by FavoritesRepository.getFavorite(context)
            .collectAsState(initial = emptySet())
        val isFavorite = currentSong.id in favorites
        val scope = rememberCoroutineScope()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentSong.title,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = currentSong.artist ?: "خواننده نامشخص",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        FavoritesRepository.toggleFavorite(context, currentSong.id)
                    }
                }
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "افزودن به علاقه‌مندی",
                    tint = if (isFavorite) Color.Red else Color.Gray
                )
            }
        }
        //پایان این را بزار


        Spacer(modifier = Modifier.height(24.dp))



        Text(
            text = "لینک دانلود: ${currentSong.downloadUrl}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ====== دکمه‌ی پخش ورژن اصلی ======
        if(currentSong.downloadUrl.isNotBlank()){
            Text(
                text = "پخش آنلاین ورژن اصلی",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            PlayButton(
                url = currentSong.downloadUrl,
                title = currentSong.title,
                artist = currentSong.artist,
                imageUrl = currentSong.imageUrl,
                label = "پخش ورژن اصلی"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        // ====== لیست ورژن‌ها ======
        if(currentSong.versions.isNotEmpty()){
            Text(
                text = "ورژن های موجود",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            currentSong.versions.forEach{version ->
                VersionRow(
                    version = version,
                    title = currentSong.title,
                    artist = currentSong.artist,
                    imageUrl = currentSong.imageUrl
                )
                Spacer(modifier = Modifier.height(8.dp))
            
            }
            
        }
        
        Spacer(modifier = Modifier.height(32.dp))









    }
}

@Composable
fun PlayButton(
    url: String,
    title: String,
    artist: String?,
    imageUrl: String?,
    label: String
){
    val context = LocalContext.current
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val currentUrl by PlayerManager.currentUrl.collectAsState()
    val playingThis = isPlaying && currentUrl == null


    Button(
        onClick = {
                  PlayerManager.togglePlayPause(context , url,  title, artist, imageUrl )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isPlaying) "توقف" else label)

    }
}

@Composable
fun VersionRow(
    version: SongVersion,
    title: String,
    artist: String?,
    imageUrl: String?
){
    val context = LocalContext.current
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val currentUrl by PlayerManager.currentUrl.collectAsState()
    val playingThis = isPlaying && currentUrl == version.url


    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${version.labelFa} (${version.labelEn})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                PlayerManager.togglePlayPause(
                    context,
                    version.url,
                    "$title - ${version.labelFa}",
                    artist,
                    imageUrl
                )
            }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}