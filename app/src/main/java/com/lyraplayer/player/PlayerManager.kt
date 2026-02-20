package com.lyraplayer.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@androidx.annotation.OptIn(UnstableApi::class)
class PlayerManager(context: Context) {

    private val appContext = context.applicationContext

    private val exoPlayer = ExoPlayer.Builder(appContext)
        .setRenderersFactory(
            DefaultRenderersFactory(appContext).apply {
                setExtensionRendererMode(
                    DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                )
            }
        )
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var usingFallback = false
    private var currentUri: String = ""

    private var onSongComplete: (() -> Unit)? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    init {
        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {

                if (state == Player.STATE_READY) {
                    _isPlaying.value = exoPlayer.isPlaying
                }

                if (state == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    onSongComplete?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                _isPlaying.value = false
                playWithMediaPlayer(currentUri)
            }
        })
    }

    fun play(uri: String) {
        currentUri = uri
        usingFallback = false

        mediaPlayer?.release()
        mediaPlayer = null

        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        _isPlaying.value = true
    }

    private fun playWithMediaPlayer(uri: String) {
        usingFallback = true

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(appContext, Uri.parse(uri))
                prepare()
                start()

                setOnCompletionListener {
                    _isPlaying.value = false
                    onSongComplete?.invoke()
                }

                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    true
                }
            }

            _isPlaying.value = true

        } catch (e: Exception) {
            _isPlaying.value = false
        }
    }

    fun pause() {
        if (usingFallback) {
            mediaPlayer?.pause()
        } else {
            exoPlayer.pause()
        }
        _isPlaying.value = false
    }

    fun resume() {
        if (usingFallback) {
            mediaPlayer?.start()
        } else {
            exoPlayer.play()
        }
        _isPlaying.value = true
    }

    fun seekTo(position: Long) {
        if (usingFallback) {
            mediaPlayer?.seekTo(position.toInt())
        } else {
            exoPlayer.seekTo(position)
        }
    }

    fun getCurrentPosition(): Long {
        return if (usingFallback) {
            mediaPlayer?.currentPosition?.toLong() ?: 0L
        } else {
            exoPlayer.currentPosition
        }
    }

    fun getDuration(): Long {
        return if (usingFallback) {
            mediaPlayer?.duration?.toLong()?.coerceAtLeast(0L) ?: 0L
        } else {
            exoPlayer.duration.coerceAtLeast(0L)
        }
    }

    fun setOnSongCompleteListener(listener: () -> Unit) {
        onSongComplete = listener
    }

    fun release() {
        exoPlayer.release()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}