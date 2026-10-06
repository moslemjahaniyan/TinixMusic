package com.tinixmusic.tinixmusic2


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(artistName: String?, navController: NavController) {
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }


    val context = LocalContext.current



    LaunchedEffect(artistName) {
        try {
            songs = if (artistName != null) {
                SongRepository.getSongByArtist(artistName)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            // نادیده بگیر
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(artistName ?: "خواننده") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            songs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("هیچ آهنگی از این خواننده پیدا نشد")
                }
            }
            else -> {
                // 👇 دکمه‌ی «پخش همه»
                Button(
                    onClick = {
                        PlayerManager.playPlaylist(context, songs)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پخش همه (${songs.size} آهنگ)")
                }

                // 👇 لیست آهنگ‌ها (بدون padding چون Column خودش padding داره)
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(songs) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                navController.navigate("songDetail/${song.id}")
                            }
                        )
                    }
                }
            }


                //بلوک زیر با بلوک بالا جایگزین شد
        /* {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(songs) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                navController.navigate("songDetail/${song.id}")
                            }
                        )
                    }
                }
            }
            */
        }
    }
}