package com.tinixmusic.tinixmusic2



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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SongDetailScreen(songId: String?, navController: NavController) {
    var song by remember { mutableStateOf<Song?>(null) }
    var isLoading by remember { mutableStateOf(true)    }



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

    if (song == null){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text("آهنگ پیدا نشد")
        }
        return
    }

    val currentSong = song!!

    Column(modifier = Modifier.padding(16.dp)) {
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "لینک دانلود: ${currentSong.downloadUrl}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // دکمه‌ی پخش (فعلاً بدون عملکرد)
        Button(onClick = { /* بعداً */ }) {
            Text("پخش آهنگ")
        }
    }
}