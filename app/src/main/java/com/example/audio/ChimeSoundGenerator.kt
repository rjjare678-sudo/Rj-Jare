package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.sin

object ChimeSoundGenerator {
    private const val SAMPLE_RATE = 44100

    suspend fun playBellChime() = withContext(Dispatchers.Default) {
        try {
            val duration = 1.2 // seconds
            val numSamples = (duration * SAMPLE_RATE).toInt()
            val sample = ShortArray(numSamples)
            
            // Frequencies for a beautiful physical bell chord:
            // 880.0 Hz (A5), 1100.0 Hz (C#6 Major third), 1320.0 Hz (E6 Fifth), and a high sparkle overtone at 2200.0 Hz
            val freq1 = 880.0
            val freq2 = 1100.0
            val freq3 = 1320.0
            val freq4 = 2200.0
            
            for (i in 0 until numSamples) {
                val t = i.toDouble() / SAMPLE_RATE
                
                // Exponential decay envelopes: base ring decays slowly, sparkle overtones decay fast
                val decay1 = exp(-t * 2.2)
                val decay2 = exp(-t * 3.5)
                val decay3 = exp(-t * 5.0)
                val decay4 = exp(-t * 10.0)
                
                val wave = (0.40 * sin(2.0 * Math.PI * freq1 * t) * decay1) +
                           (0.25 * sin(2.0 * Math.PI * freq2 * t) * decay2) +
                           (0.20 * sin(2.0 * Math.PI * freq3 * t) * decay3) +
                           (0.15 * sin(2.0 * Math.PI * freq4 * t) * decay4)
                
                // Scale to Short max and assign
                val s = (wave * Short.MAX_VALUE).toInt()
                sample[i] = s.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            
            val bufferSize = numSamples * 2
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            
            audioTrack.write(sample, 0, numSamples)
            audioTrack.play()
            
            // Wait for duration then stop/release
            kotlinx.coroutines.delay(1300)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
