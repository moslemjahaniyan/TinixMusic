package com.tinixmusic.tinixmusic2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListScreen(navController: NavController, viewModel: SongListViewModel = viewModel()) {

    val allSongs by viewModel.songs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val favorites by FavoritesRepository.getFavorite(context).collectAsState(initial = emptySet())

    val downloadedSongs by DownloadRepository.getDownloadedSongs(context)
        .collectAsState(initial = emptyList())

    // ✅ برای این‌که بدونیم کدوم آهنگ در حال پخشه
    val playingId by PlayerManager.currentSongId.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()

    // ✅ نقشه‌ی songId → مسیر محلی برای آهنگ‌های دانلودشده
    val localPaths = remember(downloadedSongs) {
        downloadedSongs.associate { it.songId to it.localPath }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val songsBasedOnTab = when (selectedTab) {
        1 -> allSongs.filter { it.id in favorites }
        2 -> downloadedSongs.map { downloaded ->
            allSongs.find { it.id == downloaded.songId } ?: Song(
                id = downloaded.songId,
                title = downloaded.title,
                artist = downloaded.artist,
                downloadUrl = downloaded.localPath,
                imageUrl = downloaded.imageUrl
            )
        }
        else -> allSongs
    }

    val songs = songsBasedOnTab
        .asSequence()
        .filter { song ->
            if (searchQuery.isBlank()) return@filter true
            song.title.contains(searchQuery, ignoreCase = true) ||
                    (song.artist?.contains(searchQuery, ignoreCase = true) == true)
        }
        .toList()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("تینیکس موزیک") },
            actions = {
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, contentDescription = "تنظیمات")
                }
            }
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("همه آهنگ‌ها")
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("علاقه‌مندی‌ها (${favorites.size})")
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("دانلودشده‌ها")
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("جستجوی آهنگ یا خواننده...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true
        )

        if (songs.isNotEmpty()) {
            Button(
                onClick = {
                    PlayerManager.playPlaylist(context, songs)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("پخش همه")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> {
                LazyColumn {
                    items(6) {
                        ShimmerSongItem()
                    }
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("خطا: $error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.fetchSongs() }) {
                            Text("تلاش مجدد")
                        }
                    }
                }
            }
            songs.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            searchQuery.isNotBlank() -> "نتیجه‌ای برای \"$searchQuery\" یافت نشد"
                            selectedTab == 1 -> "هنوز آهنگی به علاقه‌مندی‌ها اضافه نکردی ❤️"
                            else -> "هیچ آهنگی یافت نشد"
                        }
                    )
                }
            }
            else -> {
                LazyColumn {
                    items(songs, key = { it.id }) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                navController.navigate("songDetail/${song.id}")
                            },
                            onPlayClick = {
                                PlayerManager.togglePlayPause(
                                    context = context,
                                    songId = song.id,
                                    url = song.downloadUrl,
                                    title = song.title,
                                    artist = song.artist,
                                    imageUrl = song.imageUrl,
                                    localPath = localPaths[song.id],   // ✅ اگه دانلود شده
                                    playlist = songs                   // ✅ صف = همون لیست فعلی
                                )
                            },
                            isPlayingThis = isPlaying && playingId == song.id
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    isPlayingThis: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ✅ کل کاور کلیک‌پذیر + دکمه‌ی کوچیک روی تصویر
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPlayClick() }
        ) {
            AsyncImage(
                model = song.imageUrl ?: R.drawable.music_logo,
                contentDescription = "تصویر ${song.title}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // دکمه‌ی گرد کوچیک در گوشه‌ی پایین-راست
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlayingThis) "توقف" else "پخش",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = song.artist ?: "خواننده نامشخص",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}