package com.lyraplayer.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.StayPrimaryPortrait
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.lyraplayer.data.LyricLine
import com.lyraplayer.data.Song
import com.lyraplayer.ui.components.LyricView
import com.lyraplayer.ui.viewmodel.MusicViewModel
import java.io.InputStream

@Composable
fun rememberAlbumArt(path: String): ImageBitmap? {
    return remember(path) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val bytes = retriever.embeddedPicture
            retriever.release()
            bytes?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun PlayerScreen(
    song: Song,
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onLrcSelected: (InputStream) -> Unit,
    playbackMode: MusicViewModel.PlaybackMode,
    onPlaybackModeChange: (MusicViewModel.PlaybackMode) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val view = LocalView.current
    val albumArt = rememberAlbumArt(song.path)

    var isFullscreen by remember { mutableStateOf(false) }
    var isLandscape by remember { mutableStateOf(false) }

    val lrcLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { inputStream ->
                onLrcSelected(inputStream)
            }
        }
    }

    BackHandler {
        onBack()
    }

    DisposableEffect(isFullscreen) {
        activity?.window?.let { window ->
            val controller = WindowInsetsControllerCompat(window, view)
            if (isFullscreen) {
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
        onDispose {
            activity?.window?.let { window ->
                val controller = WindowInsetsControllerCompat(window, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    DisposableEffect(isLandscape) {
        activity?.requestedOrientation = if (isLandscape)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    val bgColor = MaterialTheme.colorScheme.background
    val primary = MaterialTheme.colorScheme.primary

    // Animasi skala album art saat playing (breathing effect)
    val albumScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.04f else 1f,
        animationSpec = tween(600, easing = EaseInOutCubic),
        label = "albumScale"
    )

    if (isLandscape) {
        LandscapePlayerLayout(
            song = song,
            lyrics = lyrics,
            currentLyricIndex = currentLyricIndex,
            currentPosition = currentPosition,
            duration = duration,
            onSeek = onSeek,
            isPlaying = isPlaying,
            onPlayPause = onPlayPause,
            albumArt = albumArt,
            isFullscreen = isFullscreen,
            onFullscreenToggle = { isFullscreen = !isFullscreen },
            onPortrait = {
                isLandscape = false
                isFullscreen = false
            },
            modifier = modifier
        )
        return
    }

    // ── PORTRAIT LAYOUT ───────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        // ── BACKGROUND: Album art zoom + blur ─────────────────────────
        if (albumArt != null) {
            Image(
                bitmap = albumArt,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.3f  // zoom in
                        scaleY = 1.3f
                    }
                    .blur(28.dp)
            )
        }

        // ── OVERLAY GRADIENTS ─────────────────────────────────────────
        // Gelap merata di atas background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor.copy(alpha = if (albumArt != null) 0.55f else 1f))
        )
        // Gradient bawah lebih gelap untuk area kontrol
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1.0f to bgColor.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // ── KONTEN ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = { lrcLauncher.launch("*/*") }) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        contentDescription = "Import LRC",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── ALBUM ART: Kotak dengan rounded corner + shadow ───────
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .graphicsLayer {
                        scaleX = albumScale
                        scaleY = albumScale
                    }
                    .shadow(
                        elevation = 32.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = Color.Black.copy(alpha = 0.6f),
                        spotColor = Color.Black.copy(alpha = 0.8f)
                    )
                    .clip(RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (albumArt != null) {
                    Image(
                        bitmap = albumArt,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        primary.copy(alpha = 0.5f),
                                        primary.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = song.title.take(2).uppercase(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── SONG INFO ─────────────────────────────────────────────
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── LIRIK ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LyricView(
                    lyrics = lyrics,
                    currentIndex = currentLyricIndex,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.TopCenter)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            var isDragging by remember { mutableStateOf(false) }
// ✅ Inisialisasi dengan currentPosition, bukan 0f
            var dragValue by remember { mutableStateOf(currentPosition.toFloat()) }

// ✅ Tambah ini agar dragValue ikut update saat tidak drag
            LaunchedEffect(currentPosition) {
                if (!isDragging) {
                    dragValue = currentPosition.toFloat()
                }
            }

            // ── SEEK BAR ─────────────────────────────────────────────
            if (duration > 0) {
                Slider(
                    value = if (isDragging) dragValue else currentPosition.toFloat(),
                    onValueChange = {
                        isDragging = true
                        dragValue = it
                    },
                    onValueChangeFinished = {
                        onSeek(dragValue.toLong())
                        isDragging = false
                    },
                    valueRange = 0f..duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── KONTROL: Landscape | Play/Pause | Fullscreen ──────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Landscape
                IconButton(
                    onClick = { isLandscape = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Landscape",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Play/Pause
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(16.dp, CircleShape, spotColor = primary.copy(alpha = 0.6f))
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = bgColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Fullscreen
                IconButton(
                    onClick = { isFullscreen = !isFullscreen },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// ── LANDSCAPE LAYOUT ─────────────────────────────────────────────────────────
@Composable
fun LandscapePlayerLayout(
    song: Song,
    lyrics: List<LyricLine>,
    currentLyricIndex: Int,
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    albumArt: ImageBitmap?,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    onPortrait: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Background blur
        if (albumArt != null) {
            Image(
                bitmap = albumArt,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleX = 1.3f; scaleY = 1.3f }
                    .blur(28.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor.copy(alpha = if (albumArt != null) 0.6f else 1f))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // ── KIRI ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(24.dp, RoundedCornerShape(16.dp),
                            spotColor = Color.Black.copy(alpha = 0.7f))
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (albumArt != null) {
                        Image(
                            bitmap = albumArt,
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = song.title.take(2).uppercase(),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(1.dp)
                    .fillMaxHeight(0.8f)
                    .align(Alignment.CenterVertically)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            // ── KANAN ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LyricView(
                        lyrics = lyrics,
                        currentIndex = currentLyricIndex,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.TopCenter)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.BottomCenter)
                    )
                }

                var isDragging by remember { mutableStateOf(false) }
// ✅ Inisialisasi dengan currentPosition, bukan 0f
                var dragValue by remember { mutableStateOf(currentPosition.toFloat()) }

// ✅ Tambah ini agar dragValue ikut update saat tidak drag
                LaunchedEffect(currentPosition) {
                    if (!isDragging) {
                        dragValue = currentPosition.toFloat()
                    }
                }

                if (duration > 0) {
                    Slider(
                        value = if (isDragging) dragValue else currentPosition.toFloat(),
                        onValueChange = {
                            isDragging = true
                            dragValue = it
                        },
                        onValueChangeFinished = {
                            onSeek(dragValue.toLong())
                            isDragging = false
                        },
                        valueRange = 0f..duration.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(currentPosition),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f))
                        Text(formatTime(duration),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPortrait,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(Icons.Default.StayPrimaryPortrait, "Portrait",
                            tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(12.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = bgColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(
                        onClick = onFullscreenToggle,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}