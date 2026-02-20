package com.lyraplayer.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyraplayer.data.LyricLine

@Composable
fun LyricView(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Ukur tinggi container untuk hitung offset tengah
    var viewportHeight by remember { mutableStateOf(0) }

    LaunchedEffect(currentIndex) {
        if (currentIndex >= 0 && lyrics.isNotEmpty()) {
            // +1 karena ada item spacer di index 0
            val targetIndex = currentIndex + 1
            val offset = -(viewportHeight / 2) + 60 // geser ke tengah layar
            listState.animateScrollToItem(
                index = targetIndex,
                scrollOffset = offset
            )
        }
    }

    if (lyrics.isEmpty()) {
        Text(
            text = "No lyrics available\nImport an LRC file",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.onGloballyPositioned { coordinates ->
            viewportHeight = coordinates.size.height
        }
    ) {

        // Top spacer
        item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 48.dp)) }

        itemsIndexed(lyrics) { index, line ->

            val isActive = index == currentIndex
            val isNear = kotlin.math.abs(index - currentIndex) <= 1

            // Warna animasi
            val animatedColor by animateColorAsState(
                targetValue = when {
                    isActive -> MaterialTheme.colorScheme.onBackground
                    isNear   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else     -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                },
                animationSpec = tween(400),
                label = "lyricColor"
            )

            // Alpha animasi
            val animatedAlpha by animateFloatAsState(
                targetValue = when {
                    isActive -> 1f
                    isNear   -> 0.6f
                    else     -> 0.25f
                },
                animationSpec = tween(400),
                label = "lyricAlpha"
            )

            // Scale animasi
            val animatedScale by animateFloatAsState(
                targetValue = if (isActive) 1.08f else 1f,
                animationSpec = tween(400),
                label = "lyricScale"
            )

            // Font size & weight
            val fontSize = if (isActive) 17.sp else 15.sp
            val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

            Text(
                text = line.text,
                color = animatedColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .alpha(animatedAlpha)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
            )
        }

        // Bottom spacer
        item { androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(bottom = 48.dp)) }
    }
}