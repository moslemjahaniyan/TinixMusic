package com.tinixmusic.tinixmusic2



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SongDetailScreen(songId: String?, navController: NavController) {
    val song = SongRepository.songs.find { it.id == songId }

    if (song == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("آهنگ پیدا نشد")
        }
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // دکمه‌ی بازگشت
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "بازگشت"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // تصویر آهنگ
        Image(
            painter = painterResource(id = song.imageResId ?: R.drawable.music_logo),
            contentDescription = "تصویر ${song.title}",
            modifier = Modifier.size(128.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // اطلاعات
        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = song.artist ?: "خواننده نامشخص",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "لینک دانلود: ${song.downloadUrl}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // دکمه‌ی پخش (فعلاً بدون عملکرد)
        Button(onClick = { /* بعداً */ }) {
            Text("پخش آهنگ")
        }
    }
}