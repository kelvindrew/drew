package com.example.kasa.util

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class AudioPlaybackState(
    val currentAudioId: String = "",
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0
) {
    val progressFraction: Float
        get() = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs).coerceIn(0f, 1f) else 0f
}

object AudioPlayerHelper {
    private const val TAG = "AudioPlayerHelper"
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _playbackState = MutableStateFlow(AudioPlaybackState())
    val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

    fun play(context: Context, audioId: String, audioSource: String) {
        // If clicking on the currently playing audio -> toggle pause/resume
        if (_playbackState.value.currentAudioId == audioId && mediaPlayer != null) {
            if (_playbackState.value.isPlaying) {
                pause()
            } else {
                resume()
            }
            return
        }

        // Stop any currently playing audio
        stop()

        try {
            val player = MediaPlayer()
            when {
                audioSource.startsWith("content://") -> {
                    player.setDataSource(context, Uri.parse(audioSource))
                }
                audioSource.startsWith("file://") -> {
                    player.setDataSource(audioSource.removePrefix("file://"))
                }
                File(audioSource).exists() -> {
                    player.setDataSource(audioSource)
                }
                else -> {
                    player.setDataSource(audioSource)
                }
            }

            player.prepare()
            val totalDuration = player.duration
            player.start()

            mediaPlayer = player
            _playbackState.value = AudioPlaybackState(
                currentAudioId = audioId,
                isPlaying = true,
                currentPositionMs = 0,
                totalDurationMs = totalDuration
            )

            player.setOnCompletionListener {
                stop()
            }

            startProgressTracking()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio: $audioSource", e)
            stop()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _playbackState.value = _playbackState.value.copy(isPlaying = false)
                }
            }
            progressJob?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio", e)
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let { player ->
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracking()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio", e)
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.let { player ->
                player.seekTo(positionMs)
                _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking audio", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio", e)
        } finally {
            mediaPlayer = null
            _playbackState.value = AudioPlaybackState()
        }
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = coroutineScope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition ?: 0
                val total = mediaPlayer?.duration ?: 0
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = current,
                    totalDurationMs = total
                )
                delay(100)
            }
        }
    }
}
