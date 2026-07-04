package com.arsham.dorom.util

import android.media.MediaPlayer

/** Thin wrapper around MediaPlayer for one-at-a-time local file playback. */
class AudioPlayer {
    private var player: MediaPlayer? = null

    fun play(path: String, onCompletion: () -> Unit) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(path)
            setOnCompletionListener { onCompletion() }
            prepare()
            start()
        }
    }

    fun stop() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
    }

    fun isPlaying(): Boolean = player?.isPlaying == true
}
