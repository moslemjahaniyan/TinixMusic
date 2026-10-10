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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
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

    val isShuffleOn by PlayerManager.isShuffleOn.collectAsState()
    val repeatMode by PlayerManager.repeatMode.collectAsState()

    // ===== State های سیک بار =====
    var currentPos by remember { mutableStateOf(0L) }
    var totalDur by remember { mutableStateOf(0L) }
    // وقتی کاربر در حال درگ کردن اسلایدر هست، مقدار player رو نخون
    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableStateOf(0f) }

    // ✅ هر ۵۰۰ میلی‌ثانیه موقعیت/مدت رو آپدیت کن (اگه در حال درگ نیست)
    LaunchedEffect(nowPlaying) {
        while (nowPlaying != null) {
            if (!isDragging) {
                currentPos = PlayerManager.getCurrentPosition()
                totalDur = PlayerManager.getDuration()
            }
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
                // ===== سیک بار قابل درگ =====
                if (totalDur > 0) {
                    val sliderValue = if (isDragging) dragValue else currentPos.toFloat()
                    val sliderMax = totalDur.toFloat().coerceAtLeast(1f)

                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Slider(
                            value = sliderValue.coerceIn(0f, sliderMax),
                            onValueChange = { newValue ->
                                isDragging = true
                                dragValue = newValue
                            },
                            onValueChangeFinished = {
                                PlayerManager.seekTo(dragValue.toLong())
                                currentPos = dragValue.toLong()
                                isDragging = false
                            },
                            valueRange = 0f..sliderMax,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(if (isDragging) dragValue.toLong() else currentPos),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTime(totalDur),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // قبل از لود شدن مدت، فقط یه خط نازک
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
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
                    Spacer(modifier = Modifier.width(10.dp))

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
                        onClick = { PlayerManager.toggleShuffle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "پخش تصادفی",
                            modifier = Modifier.size(20.dp),
                            tint = if (isShuffleOn) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    IconButton(
                        onClick = { PlayerManager.cycleRepeatMode() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "تکرار",
                            modifier = Modifier.size(20.dp),
                            tint = if (repeatMode != RepeatMode.OFF) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    IconButton(
                        onClick = { PlayerManager.skipPrevious() },
                        enabled = PlayerManager.hasPrevious(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "قبلی",
                            modifier = Modifier.size(22.dp)
                        )
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
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = { PlayerManager.skipNext() },
                        enabled = PlayerManager.hasNext(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "بعدی",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}


/** تبدیل میلی‌ثانیه به فرمت m:ss */
private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%d:%02d".format(m, s)
}