package com.tinixmusic.tinixmusic2



import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Delete

import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.OutlinedButton

import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import com.tinixmusic.tinixmusic2.PlayerManager.currentUrl


@Composable
fun SongDetailScreen(songId: String?, navController: NavController) {
    var allSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // آهنگی که در این صفحه نمایش داده می‌شه (با پارامتر مسیر شروع می‌شه)
    var displayedSongId by remember(songId) { mutableStateOf(songId) }

    // آهنگ در حال پخش و آخرین مقداری که دیدیم
    val currentPlayingSongId by PlayerManager.currentSongId.collectAsState()
    var lastKnownPlayingId by remember { mutableStateOf(currentPlayingSongId) }

    // ۱. لود کردن کل آهنگ‌ها یه بار
    LaunchedEffect(Unit) {
        try {
            allSongs = SongRepository.getSong()
        } catch (e: Exception) {
            // نادیده بگیر
        } finally {
            isLoading = false
        }
    }

    // ۲. هر بار آهنگ در حال پخش به یه آهنگ *جدید* تغییر کرد، صفحه رو همراهش کن
    LaunchedEffect(currentPlayingSongId, allSongs) {
        if (allSongs.isEmpty()) return@LaunchedEffect
        val playingId = currentPlayingSongId ?: return@LaunchedEffect

        // فقط اگه واقعاً از آهنگ قبلی به یه آهنگ جدید رفته باشه (نه اینکه کاربر تازه اومده)
        if (playingId != lastKnownPlayingId && playingId != displayedSongId) {
            if (allSongs.any { it.id == playingId }) {
                displayedSongId = playingId
            }
        }
        lastKnownPlayingId = playingId
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // آهنگ فعلی رو از روی displayedSongId پیدا کن
    val currentSong = allSongs.find { it.id == displayedSongId }
    if (currentSong == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("آهنگ پیدا نشد")
        }
        return
    }

    // ⬇️⬇️⬇️ از اینجا به بعد کد قبلی خودت بدون تغییر ⬇️⬇️⬇️
    // (Column، AsyncImage، favorites، PlayButton، LyricsSection، VersionRow و ...)
    // نکته: چون currentSong حالا از displayedSongId حساب می‌شه،
    // همه‌ی UI خودکار با آهنگ جدید آپدیت می‌شه.

    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)) {
        // ... بقیه بدون تغییر {
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

                //اینو تغییر دادیم به زیریش
                /*Text(
                    text = currentSong.artist ?: "خواننده نامشخص",
                    style = MaterialTheme.typography.bodyLarge
                )*/
                Text(
                    text = currentSong.artist ?: "خواننده نامشخص",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        if (currentSong.artist != null && currentSong.artist != "نامشخص") {
                            navController.navigate("artist/${currentSong.artist}")
                        }
                    }
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

        // ====== دکمه‌ی پخش ورژن اصلی ======
        /* کد زیر با زیری تر جایگزین شد
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

         */

        if(currentSong.downloadUrl.isNotBlank()){
            Text(
                text = "پخش آنلاین ورژن اصلی",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))



            var isDownloaded by remember { mutableStateOf(false) }
            var localPath by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(currentSong.id) {
                isDownloaded = DownloadRepository.isDownloaded(context, currentSong.id)
                localPath = if (isDownloaded) {
                    DownloadRepository.getLocalPath(context, currentSong.id)
                } else {
                    null
                }
            }



            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // دکمه‌ی Previous
                OutlinedButton(
                    onClick = { PlayerManager.skipPrevious() },
                    modifier = Modifier.weight(1f),
                    enabled = PlayerManager.hasPrevious()
                ) {
                    Text("قبلی")
                }

                // دکمه‌ی Play/Pause
                PlayButton(
                    songId = currentSong.id,
                    url = currentSong.downloadUrl,
                    title = currentSong.title,
                    artist = currentSong.artist,
                    imageUrl = currentSong.imageUrl,
                    label = "پخش",
                    localPath = localPath,
                    playlist = allSongs           // ✅ اضافه شد
                )

                // دکمه‌ی Next
                OutlinedButton(
                    onClick = { PlayerManager.skipNext() },
                    modifier = Modifier.weight(1f),
                    enabled = PlayerManager.hasNext()
                ) {
                    Text("بعدی")
                }



            }

            Spacer(modifier = Modifier.height(8.dp))

// Shuffle و Repeat
            val isShuffleOn by PlayerManager.isShuffleOn.collectAsState()
            val repeatMode by PlayerManager.repeatMode.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Shuffle
                IconButton(onClick = { PlayerManager.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "پخش تصادفی",
                        tint = if (isShuffleOn) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                // Repeat
                IconButton(onClick = { PlayerManager.cycleRepeatMode() }) {
                    Icon(
                        imageVector = when (repeatMode) {
                            RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        contentDescription = "تکرار",
                        tint = if (repeatMode != RepeatMode.OFF) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }



            Spacer(modifier = Modifier.height(16.dp))

// دکمه‌ی اشتراک‌گذاری
            OutlinedButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "🎵 ${currentSong.title} - ${currentSong.artist}\n\n" +
                                    "📥 دانلود از تینیکس موزیک:\n" +
                                    "https://tinixmusic.ir/?p=${currentSong.id}"
                        )
                        putExtra(Intent.EXTRA_SUBJECT, currentSong.title)
                    }
                    val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری با...")
                    context.startActivity(chooser)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اشتراک‌گذاری")
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isDownloaded) {
                    // دکمه‌ی حذف دانلود
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                DownloadRepository.deleteDownload(context, currentSong.id)
                                isDownloaded = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف دانلود")
                    }
                } else {
                    // دکمه‌ی دانلود
                    Button(
                        onClick = {
                            DownloadRepository.startDownload(
                                context = context,
                                song = currentSong,
                                url = currentSong.downloadUrl
                            )
                            Toast.makeText(context, "دانلود در پس‌زمینه شروع شد", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("دانلود")
                    }
                }
            }












            Spacer(modifier = Modifier.height(24.dp))
            LyricsSection(song = currentSong)
            Spacer(modifier = Modifier.height(32.dp))




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
                    songId = currentSong.id,  // ✅ اضافه شد
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
    songId: String,
    url: String,
    title: String,
    artist: String?,
    imageUrl: String?,
    label: String,
    localPath: String? = null,
    playlist: List<Song>? = null,     // ✅ جدید
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val currentSongId by PlayerManager.currentSongId.collectAsState()
    val playingThis = isPlaying && currentSongId == songId

    Button(
        onClick = {
            PlayerManager.togglePlayPause(
                context = context,
                songId = songId,
                url = url,
                title = title,
                artist = artist,
                imageUrl = imageUrl,
                localPath = localPath,
                playlist = playlist       // ✅ پاس بده
            )
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (playingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (playingThis) "توقف" else label)
    }
}

@Composable
fun VersionRow(
    songId: String,
    version: SongVersion,
    title: String,
    artist: String?,
    imageUrl: String?
) {
    val context = LocalContext.current
    val isPlaying by PlayerManager.isPlaying.collectAsState()
    val currentSongId by PlayerManager.currentSongId.collectAsState()
    // برای هر ورژن، یه songId مجازی بساز تا از نسخه‌ی اصلی جدا باشه
    val versionMediaId = "${songId}_${version.labelEn}"
    val playingThis = isPlaying && currentSongId == versionMediaId

    Row(
        modifier = Modifier
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
                    context = context,
                    songId = versionMediaId,   // ✅ آیدی یونیک برای هر ورژن
                    url = version.url,
                    title = "$title - ${version.labelFa}",
                    artist = artist,
                    imageUrl = imageUrl
                )
            }
        ) {
            Icon(
                imageVector = if (playingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}