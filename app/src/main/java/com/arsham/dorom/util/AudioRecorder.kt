package com.arsham.dorom.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Thin wrapper around MediaRecorder for short voice notes (guitar practice, daily feedback). */
class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun start(file: File) {
        outputFile = file
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()
        recorder = r
    }

    /** Returns the recorded file, or null if nothing was recording / it failed. */
    fun stop(): File? {
        return try {
            recorder?.apply { stop(); release() }
            outputFile
        } catch (e: Exception) {
            null
        } finally {
            recorder = null
        }
    }

    fun cancel() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        outputFile?.delete()
    }
}
