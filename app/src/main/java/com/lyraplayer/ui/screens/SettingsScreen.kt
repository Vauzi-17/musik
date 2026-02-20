package com.lyraplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = MaterialTheme.colorScheme.surfaceVariant

    var darkTheme by remember { mutableStateOf(true) }
    var dynamicColor by remember { mutableStateOf(false) }
    var showLyricHighlight by remember { mutableStateOf(true) }
    var autoplayFolder by remember { mutableStateOf(false) }
    var skipSilence by remember { mutableStateOf(false) }
    var showAlbumArtBackground by remember { mutableStateOf(true) }

    BackHandler {
        onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = onSurfaceColor)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // SECTION: TAMPILAN
            item { SettingsSectionHeader(title = "Tampilan", icon = Icons.Default.Palette) }
            item {
                SettingsCard(containerColor = containerColor) {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode, iconTint = primaryColor,
                        title = "Tema Gelap", subtitle = "Gunakan tampilan dark mode",
                        checked = darkTheme, onCheckedChange = { darkTheme = it }
                    )
                    SettingsDivider()
                    SettingsToggleItem(
                        icon = Icons.Default.ColorLens, iconTint = primaryColor,
                        title = "Warna Dinamis", subtitle = "Ikuti warna tema sistem (Android 12+)",
                        checked = dynamicColor, onCheckedChange = { dynamicColor = it }
                    )
                    SettingsDivider()
                    SettingsToggleItem(
                        icon = Icons.Default.Wallpaper, iconTint = primaryColor,
                        title = "Background Album Art", subtitle = "Tampilkan album art sebagai background player",
                        checked = showAlbumArtBackground, onCheckedChange = { showAlbumArtBackground = it }
                    )
                }
            }

            // SECTION: LIRIK
            item { SettingsSectionHeader(title = "Lirik", icon = Icons.Default.Lyrics) }
            item {
                SettingsCard(containerColor = containerColor) {
                    SettingsToggleItem(
                        icon = Icons.Default.FormatColorText, iconTint = primaryColor,
                        title = "Highlight Lirik Aktif", subtitle = "Animasi warna pada lirik yang sedang diputar",
                        checked = showLyricHighlight, onCheckedChange = { showLyricHighlight = it }
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.TextFields, iconTint = primaryColor,
                        title = "Ukuran Teks Lirik", subtitle = "Sedang", onClick = {}
                    )
                }
            }

            // SECTION: PEMUTARAN
            item { SettingsSectionHeader(title = "Pemutaran", icon = Icons.Default.PlayCircle) }
            item {
                SettingsCard(containerColor = containerColor) {
                    SettingsToggleItem(
                        icon = Icons.Default.SkipNext, iconTint = primaryColor,
                        title = "Autoplay Folder", subtitle = "Lanjut putar lagu berikutnya dalam folder",
                        checked = autoplayFolder, onCheckedChange = { autoplayFolder = it }
                    )
                    SettingsDivider()
                    SettingsToggleItem(
                        icon = Icons.Default.VolumeOff, iconTint = primaryColor,
                        title = "Lewati Keheningan", subtitle = "Otomatis skip bagian hening pada lagu",
                        checked = skipSilence, onCheckedChange = { skipSilence = it }
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.Speed, iconTint = primaryColor,
                        title = "Kecepatan Putar", subtitle = "1.0x", onClick = {}
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.GraphicEq, iconTint = primaryColor,
                        title = "Equalizer", subtitle = "Atur kualitas suara", onClick = {}
                    )
                }
            }

            // SECTION: PENYIMPANAN
            item { SettingsSectionHeader(title = "Penyimpanan", icon = Icons.Default.Storage) }
            item {
                SettingsCard(containerColor = containerColor) {
                    SettingsClickItem(
                        icon = Icons.Default.FolderOpen, iconTint = primaryColor,
                        title = "Folder Musik", subtitle = "Kelola folder yang dipindai", onClick = {}
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.Refresh, iconTint = primaryColor,
                        title = "Perbarui Library", subtitle = "Scan ulang file musik di perangkat", onClick = {}
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.DeleteSweep, iconTint = MaterialTheme.colorScheme.error,
                        title = "Hapus Cache", subtitle = "Bersihkan data sementara aplikasi", onClick = {}
                    )
                }
            }

            // SECTION: TENTANG
            item { SettingsSectionHeader(title = "Tentang", icon = Icons.Default.Info) }
            item {
                SettingsCard(containerColor = containerColor) {
                    SettingsClickItem(
                        icon = Icons.Default.MusicNote, iconTint = primaryColor,
                        title = "Versi Aplikasi",
                        subtitle = "1.0.0", // Ganti dengan BuildConfig.VERSION_NAME
                        onClick = {}
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.Code, iconTint = primaryColor,
                        title = "Lisensi Open Source", subtitle = "Library yang digunakan", onClick = {}
                    )
                    SettingsDivider()
                    SettingsClickItem(
                        icon = Icons.Default.BugReport, iconTint = primaryColor,
                        title = "Laporkan Bug", subtitle = "Bantu kami memperbaiki aplikasi", onClick = {}
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.2.sp
        )
    }
}

@Composable
private fun SettingsCard(
    containerColor: androidx.compose.ui.graphics.Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 0.5.dp
    )
}