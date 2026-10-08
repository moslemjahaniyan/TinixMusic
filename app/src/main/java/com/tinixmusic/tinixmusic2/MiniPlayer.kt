package com.tinixmusic.tinixmusic2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun MiniPlayer(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nowPlaying by PlayerManager.nowPlaying.collectAsState()
    val isPlaying by PlayerManager.isPlaying.collectAsState()

    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(nowPlaying) {
        while (nowPlaying != null) {
            val pos = PlayerManager.getCurrentPosition()
            val dur = PlayerManager.getDuration()
            progress = if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
            delay(500)
        }
    }

    AnimatedVisibility(
        visible = nowPlaying != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val np = nowPlaying ?: return@AnimatedVisibility
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clickable {
                    if (np.songId.isNotBlank()) {
                        navController.navigate("songDetail/${np.songId}") {
                            launchSingleTop = true
                        }
                    }
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = np.imageUrl ?: R.drawable.music_logo,
                        contentDescription = null,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = np.title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = np.artist ?: "نامشخص",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { PlayerManager.skipPrevious() },
                        enabled = PlayerManager.hasPrevious()
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "قبلی")
                    }
                    IconButton(
                        onClick = {
                            PlayerManager.togglePlayPause(
                                context = context,
                                songId = np.songId,
                                url = np.url,
                                title = np.title,
                                artist = np.artist,
                                imageUrl = np.imageUrl
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش"
                        )
                    }
                    IconButton(
                        onClick = { PlayerManager.skipNext() },
                        enabled = PlayerManager.hasNext()
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "بعدی")
                    }
                }
            }
        }
    }
}