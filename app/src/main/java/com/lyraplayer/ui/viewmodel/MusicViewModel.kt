package com.lyraplayer.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lyraplayer.data.MusicRepository
import com.lyraplayer.data.Song
import com.lyraplayer.data.LyricLine
import com.lyraplayer.data.LrcParser
import com.lyraplayer.player.PlayerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application.contentResolver)
    private val playerManager = PlayerManager(application)

    // 🎵 RAW SONG LIST
    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    // 🔎 SEARCH
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 🔃 SORT
    private val _sortOption = MutableStateFlow(SortOption.TITLE)
    val sortOption: StateFlow<SortOption> = _sortOption

    // 🎯 FILTERED RESULT (SEARCH + SORT)
    val filteredSongs: StateFlow<List<Song>> =
        combine(_songs, _searchQuery, _sortOption) { songs, query, sort ->

            val filtered = songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }

            when (sort) {
                SortOption.TITLE -> filtered.sortedBy { it.title }
                SortOption.ARTIST -> filtered.sortedBy { it.artist }
                SortOption.DURATION -> filtered.sortedBy { it.duration }
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    // 🎵 CURRENT SONG
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    // ⏱ CURRENT POSITION
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    // ⏱ DURATION
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    // ▶️ PLAYING STATE
    val isPlaying: StateFlow<Boolean> = playerManager.isPlaying

    // 🎶 LYRICS
    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics

    // 🎯 CURRENT LYRIC INDEX
    val currentLyricIndex: StateFlow<Int> =
        combine(_currentPosition, _lyrics) { position, lyrics ->
            lyrics.indexOfLast { it.time <= position }
                .coerceAtLeast(0)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    // 🔧 INIT
    init {
        loadSongs()

        // Update progress only when playing
        viewModelScope.launch {
            while (true) {
                _currentPosition.value = playerManager.getCurrentPosition()
                _duration.value = playerManager.getDuration()  // ✅ selalu update
                delay(500)
            }
        }
    }

    // 🔎 SEARCH UPDATE
    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    // 🔃 SORT UPDATE
    fun updateSort(option: SortOption) {
        _sortOption.value = option
    }

    enum class SortOption {
        TITLE,
        ARTIST,
        DURATION
    }

    // 📂 LOAD SONGS
    private fun loadSongs() {
        viewModelScope.launch {
            _songs.value = repository.getAllSongs()
        }
    }

    // ▶️ PLAY
    fun playSong(song: Song) {
        _currentSong.value = song
        _duration.value = 0L  // reset dulu
        _currentPosition.value = 0L
        playerManager.play(song.uri)
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            playerManager.pause()
        } else {
            _currentSong.value?.let {
                playerManager.resume()
            }
        }
    }

    // MusicViewModel.kt
    fun seekTo(position: Long) {
        playerManager.seekTo(position)
        _currentPosition.value = position // ✅ langsung update tanpa tunggu loop
    }

    // 🎶 LOAD LRC
    fun loadLrc(inputStream: InputStream) {
        val text = inputStream.bufferedReader().use { it.readText() }
        _lyrics.value = LrcParser.parse(text)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
