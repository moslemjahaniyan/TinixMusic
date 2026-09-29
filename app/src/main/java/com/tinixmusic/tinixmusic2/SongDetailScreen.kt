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
        Text(
            text = currentSong.title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = currentSong.artist ?: "خواننده نامشخص",
            style = MaterialTheme.typography.bodyLarge
        )
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
                url = currentSong.downloadUrl, label = "پخش ورژن اصلی"
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
                VersionRow(version = version)
                Spacer(modifier = Modifier.height(8.dp))
            
            }
            
        }
        
        Spacer(modifier = Modifier.height(32.dp))









    }
}

@Composable
fun PlayButton(url: String, label: String){
    var context = LocalContext.current
    var isPlaying by remember {
        mutableStateOf(PlayerManager.isPlaying(url))
    }

    // هر ثانیه وضعیت پخش رو چک کن
    LaunchedEffect(url){
        while(true){
            isPlaying = PlayerManager.isPlaying(url)
            kotlinx.coroutines.delay(500)

        }
    }

    Button(
        onClick = {
            PlayerManager.playSong(context, url)
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
fun VersionRow(version: SongVersion){
    val context = LocalContext.current
    var isPlaying by remember {
        mutableStateOf(PlayerManager.isPlaying(version.url))
    }

    LaunchedEffect(version.url){
        while (true){
            isPlaying = PlayerManager.isPlaying(version.url)
            kotlinx.coroutines.delay(500)
        }
    }

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "${version.labelFa} (${version.labelEn})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                PlayerManager.playSong(context, version.url)
            }
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}