package com.jk.positraRx.scanner

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer

class FeedbackUtil(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var beepEnabled = true
    init {
        initializeBeepSound()
    }
    fun setBeepEnabled(enabled: Boolean) {
        beepEnabled = enabled
    }

    private fun initializeBeepSound() {
        if (!beepEnabled) return
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.notification_sound).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
            }
        } catch (e: Exception) {
            mediaPlayer = null
        }
    }

    fun playBeepSound() {
        if (!beepEnabled) return
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}