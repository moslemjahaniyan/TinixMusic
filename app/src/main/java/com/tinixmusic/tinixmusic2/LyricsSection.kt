package com.tinixmusic.tinixmusic2


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LyricsSection(song: Song) {
    var lyrics by remember { mutableStateOf<List<LyricLine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (!showLyrics) {
        Button(
            onClick = {
                showLyrics = true
                isLoading = true
                scope.launch {
                    val fetched = LyricsRepository.fetchLyrics(
                        song.title,
                        song.artist ?: ""
                    )
                    val translated = fetched.map { line ->
                        line.copy(translation = LyricsRepository.translateLine(line.text))
                    }
                    lyrics = translated
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("نمایش متن آهنگ")
        }
        return
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        lyrics.isEmpty() -> {
            Text(
                "متن آهنگ پیدا نشد",
                modifier = Modifier.padding(16.dp)
            )
        }
        else -> {
            var currentPos by remember { mutableStateOf(0L) }
            LaunchedEffect(Unit) {
                while (true) {
                    currentPos = PlayerManager.getCurrentPosition()
                    delay(200)
                }
            }

            val activeIndex = lyrics.indexOfLast { it.timeMs <= currentPos }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .padding(8.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = line.text,
                            color = if (index == activeIndex) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (index == activeIndex) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            }
                        )
                        if (line.translation.isNotBlank()) {
                            Text(
                                text = line.translation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}