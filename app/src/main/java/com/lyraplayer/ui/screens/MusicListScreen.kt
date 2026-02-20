package com.lyraplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.lyraplayer.data.Song
import com.lyraplayer.ui.viewmodel.MusicViewModel
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Settings

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListScreen(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSortChange: (MusicViewModel.SortOption) -> Unit,
    playbackMode: MusicViewModel.PlaybackMode,           // ✅ tambah
    onPlaybackModeChange: (MusicViewModel.PlaybackMode) -> Unit, // ✅ tambah
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedSort by remember { mutableStateOf(MusicViewModel.SortOption.TITLE) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {

        // ── TOP HEADER ──────────────────────────────────────────────
        // Ganti bagian TOP HEADER dari Column menjadi:
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 🔹 Kiri: Title
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariantColor
                )
            }

            // 🔹 Kanan: Playback + Settings
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val isActive = playbackMode != MusicViewModel.PlaybackMode.OFF

                IconButton(
                    onClick = {
                        onPlaybackModeChange(
                            when (playbackMode) {
                                MusicViewModel.PlaybackMode.OFF -> MusicViewModel.PlaybackMode.AUTOPLAY_FOLDER
                                MusicViewModel.PlaybackMode.AUTOPLAY_FOLDER -> MusicViewModel.PlaybackMode.REPEAT_ONE
                                MusicViewModel.PlaybackMode.REPEAT_ONE -> MusicViewModel.PlaybackMode.OFF
                            }
                        )
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = when (playbackMode) {
                            MusicViewModel.PlaybackMode.OFF -> Icons.Default.Repeat
                            MusicViewModel.PlaybackMode.AUTOPLAY_FOLDER -> Icons.Default.Folder
                            MusicViewModel.PlaybackMode.REPEAT_ONE -> Icons.Default.RepeatOne
                        },
                        contentDescription = "Playback Mode",
                        tint = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // ── SEARCH BAR ──────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search songs or artists...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = onSurfaceVariantColor
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        )

        // ── SORT ROW ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null,
                tint = onSurfaceVariantColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Sort:",
                style = MaterialTheme.typography.labelMedium,
                color = onSurfaceVariantColor
            )

            listOf(
                MusicViewModel.SortOption.TITLE to "Title",
                MusicViewModel.SortOption.ARTIST to "Artist",
                MusicViewModel.SortOption.DURATION to "Duration"
            ).forEach { (option, label) ->
                val isSelected = selectedSort == option
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedSort = option
                        onSortChange(option)
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        // ── EMPTY STATE ─────────────────────────────────────────────
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = onSurfaceVariantColor.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No songs found",
                        style = MaterialTheme.typography.titleMedium,
                        color = onSurfaceVariantColor
                    )
                    Text(
                        text = "Try adjusting your search",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor.copy(alpha = 0.6f)
                    )
                }
            }
            return
        }

        // ── GROUPED SONG LIST ────────────────────────────────────────
        val groupedSongs = songs.groupBy {
            File(it.path).parentFile?.name ?: "Unknown"
        }

        val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            groupedSongs.forEach { (folderName, songsInFolder) ->

                val isExpanded = expandedFolders[folderName] ?: false

                // 📂 Folder Header
                item(key = "folder_$folderName") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedFolders[folderName] = !isExpanded
                            },
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isExpanded) 0.dp else 12.dp,
                            bottomEnd = if (isExpanded) 0.dp else 12.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = containerColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primaryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = folderName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = onSurfaceColor
                                    )
                                    Text(
                                        text = "${songsInFolder.size} songs",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onSurfaceVariantColor
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = onSurfaceVariantColor
                            )
                        }
                    }
                }

                // 🎵 Animated Song List
                item(key = "songs_$folderName") {
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(250)),
                        exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(250))
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 12.dp,
                                bottomEnd = 12.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = containerColor.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                songsInFolder.forEachIndexed { index, song ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSongClick(song) }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Track number / music icon
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(primaryColor.copy(alpha = 0.08f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = primaryColor.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Title + Artist
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = onSurfaceColor
                                            )
                                            Text(
                                                text = song.artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = onSurfaceVariantColor
                                            )
                                        }

                                        // Duration
                                        Text(
                                            text = formatDuration(song.duration),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = onSurfaceVariantColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Space between folders
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}