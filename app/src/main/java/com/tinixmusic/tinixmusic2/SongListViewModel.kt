package com.tinixmusic.tinixmusic2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SongListViewModel : ViewModel(){
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init{
        fetchSongs()
    }
    fun fetchSongs(){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _songs.value = SongRepository.getSong()
            } catch (e: Exception){
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}