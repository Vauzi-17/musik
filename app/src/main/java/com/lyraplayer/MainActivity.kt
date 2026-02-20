package com.lyraplayer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lyraplayer.ui.components.MiniPlayer
import com.lyraplayer.ui.screens.MusicListScreen
import com.lyraplayer.ui.screens.PlayerScreen
import com.lyraplayer.ui.theme.LyraPlayerTheme
import com.lyraplayer.ui.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 100)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }

        setContent {
            LyraPlayerTheme {

                val songs by viewModel.filteredSongs.collectAsState()
                val searchQuery by viewModel.searchQuery.collectAsState()
                val currentSong by viewModel.currentSong.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val currentPosition by viewModel.currentPosition.collectAsState()
                val duration by viewModel.duration.collectAsState()
                val lyrics by viewModel.lyrics.collectAsState()
                val currentLyricIndex by viewModel.currentLyricIndex.collectAsState()

                var showPlayer by remember { mutableStateOf(false) }

                Scaffold(
                    contentWindowInsets = if (showPlayer)
                        WindowInsets.safeDrawing
                    else
                        WindowInsets(0),
                    bottomBar = {
                        if (!showPlayer) {
                            currentSong?.let { song ->
                                MiniPlayer(
                                    song = song,
                                    isPlaying = isPlaying,
                                    currentPosition = currentPosition,
                                    duration = duration,
                                    onPlayPause = { viewModel.togglePlayPause() },
                                    onSeek = { viewModel.seekTo(it) },
                                    onClick = { showPlayer = true }
                                )
                            }
                        }
                    }
                ) { innerPadding ->

                    val song = currentSong

                    AnimatedContent(
                        targetState = showPlayer && song != null,
                        transitionSpec = {
                            if (targetState) {
                                // Buka PlayerScreen: slide up dari bawah
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(420)
                                ) + fadeIn(
                                    animationSpec = tween(300)
                                ) togetherWith slideOutVertically(
                                    targetOffsetY = { -it / 6 },
                                    animationSpec = tween(420)
                                ) + fadeOut(
                                    animationSpec = tween(200)
                                )
                            } else {
                                // Kembali ke MusicList: slide down ke bawah
                                slideInVertically(
                                    initialOffsetY = { -it / 6 },
                                    animationSpec = tween(420)
                                ) + fadeIn(
                                    animationSpec = tween(300)
                                ) togetherWith slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(420)
                                ) + fadeOut(
                                    animationSpec = tween(200)
                                )
                            }
                        },
                        label = "playerTransition"
                    ) { isShowingPlayer ->

                        if (isShowingPlayer && song != null) {
                            PlayerScreen(
                                song = song,
                                lyrics = lyrics,
                                currentLyricIndex = currentLyricIndex,
                                currentPosition = currentPosition,
                                duration = duration,
                                isPlaying = isPlaying,
                                onPlayPause = { viewModel.togglePlayPause() },
                                onSeek = { viewModel.seekTo(it) },
                                onBack = { showPlayer = false },
                                onLrcSelected = { inputStream ->
                                    viewModel.loadLrc(inputStream)
                                },
                                modifier = Modifier
                            )
                        } else {
                            MusicListScreen(
                                songs = songs,
                                searchQuery = searchQuery,
                                onSearchChange = { viewModel.updateSearch(it) },
                                onSortChange = { viewModel.updateSort(it) },
                                onSongClick = { selectedSong ->
                                    viewModel.playSong(selectedSong)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}