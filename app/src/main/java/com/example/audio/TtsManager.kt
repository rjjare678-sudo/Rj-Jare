package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onInitialized: () -> Unit = {}
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingSpeechQueue = mutableListOf<String>()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsManager", "US English Language is not supported or missing data.")
            }
            
            // Try to find a female-sounding voice or fallback to custom voice tuning
            configureFemaleVoice()

            isInitialized = true
            onInitialized()

            // Speak any pending statements
            synchronized(pendingSpeechQueue) {
                for (text in pendingSpeechQueue) {
                    speakInternal(text)
                }
                pendingSpeechQueue.clear()
            }
        } else {
            Log.e("TtsManager", "Initialization of TextToSpeech failed.")
        }
    }

    private fun configureFemaleVoice() {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                // Look for common keywords or US English female-like voice names
                val femaleVoice = voices.firstOrNull { voice ->
                    val name = voice.name.lowercase()
                    // Android default high quality female voices often contain "female", "en-us-x-sfg", "en-us-x-local", "en-us-x-i-local"
                    (name.contains("female") || 
                     name.contains("fem") || 
                     name.contains("sfg") || 
                     name.contains("i-local") || 
                     name.contains("network")) && voice.locale.language == "en"
                }
                
                if (femaleVoice != null) {
                    tts?.voice = femaleVoice
                    Log.d("TtsManager", "Selected female voice: ${femaleVoice.name}")
                } else {
                    // Fallback to default US English and adjust pitch
                    tts?.setPitch(1.22f) // Higher pitch gives a female AI tone
                    Log.d("TtsManager", "No explicit female voice found; tuned pitch to 1.22f")
                }
            } else {
                tts?.setPitch(1.22f)
            }
            // Elegant professional rate
            tts?.setSpeechRate(0.95f) 
        } catch (e: Exception) {
            Log.e("TtsManager", "Error configuring female voice: ${e.message}")
            tts?.setPitch(1.22f)
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            speakInternal(text)
        } else {
            synchronized(pendingSpeechQueue) {
                pendingSpeechQueue.add(text)
            }
        }
    }

    private fun speakInternal(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "todo_tts_id")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("TtsManager", "Error shutting down TTS: ${e.message}")
        }
    }
}
