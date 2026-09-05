package com.example.kasa.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

object AudioRecorderHelper {
    private const val TAG = "AudioRecorderHelper"
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMs: Long = 0L
    private var recording = false

    fun isRecording(): Boolean = recording

    fun getElapsedSeconds(): Int {
        if (!recording) return 0
        return ((System.currentTimeMillis() - startTimeMs) / 1000).toInt()
    }

    fun startRecording(context: Context): Boolean {
        if (recording) {
            cancelRecording()
        }

        return try {
            val audioDir = File(context.cacheDir, "audio_notes").apply {
                if (!exists()) mkdirs()
            }
            val outputFile = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            currentOutputFile = outputFile

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            recording = true
            startTimeMs = System.currentTimeMillis()
            Log.d(TAG, "Recording started -> ${outputFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            currentOutputFile?.delete()
            currentOutputFile = null
            recording = false
            false
        }
    }

    fun stopRecording(): Pair<File, Int>? {
        if (!recording) return null
        return try {
            val durationSec = getElapsedSeconds().coerceAtLeast(1)
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            recording = false

            val file = currentOutputFile
            currentOutputFile = null
            if (file != null && file.exists() && file.length() > 0) {
                Log.d(TAG, "Recording stopped successfully: ${file.absolutePath}, $durationSec s")
                Pair(file, durationSec)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            cancelRecording()
            null
        }
    }

    fun cancelRecording() {
        try {
            if (recording) {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception cancelling recording", e)
        } finally {
            mediaRecorder = null
            recording = false
            currentOutputFile?.delete()
            currentOutputFile = null
        }
    }
}
