package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.ChimeSoundGenerator
import com.example.audio.TtsManager
import com.example.data.AppDatabase
import com.example.data.Task
import com.example.data.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    val allTasks: StateFlow<List<Task>>
    
    // Confetti burst trigger count
    private val _confettiTrigger = MutableStateFlow(0)
    val confettiTrigger: StateFlow<Int> = _confettiTrigger.asStateFlow()
    
    // Simulated User Location
    val userLocationName = MutableStateFlow("Unknown Location")
    val userLatitude = MutableStateFlow(37.7749)  // SF default
    val userLongitude = MutableStateFlow(-122.4194)

    // Triggered alerts for dialogs/banners in UI
    private val _activeTimeReminder = MutableStateFlow<Task?>(null)
    val activeTimeReminder: StateFlow<Task?> = _activeTimeReminder.asStateFlow()

    private val _activeLocationReminder = MutableStateFlow<Task?>(null)
    val activeLocationReminder: StateFlow<Task?> = _activeLocationReminder.asStateFlow()
    
    // TTS Manager
    private var ttsManager: TtsManager? = null
    private var hasSpokenWelcome = false

    private val encouragements = listOf(
        "Magnificent, sir. Another objective secured.",
        "Outstanding focus, sir. Keep this momentum.",
        "Splendid progress, sir. We are advancing rapidly.",
        "Target achieved, sir. Your efficiency is exemplary.",
        "Excellent work, sir. Directive completed.",
        "Smoothly handled, sir. Proceeding to the next task."
    )

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        
        allTasks = repository.allTasksFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
            
        // Setup TTS with rich contextual greeting
        ttsManager = TtsManager(application) {
            viewModelScope.launch {
                // Wait briefly for tasks to load to generate rich text
                delay(800)
                if (!hasSpokenWelcome) {
                    val speech = generateWelcomeSpeech(allTasks.value)
                    ttsManager?.speak(speech)
                    hasSpokenWelcome = true
                }
            }
        }

        // Start background ticker to evaluate time-based reminders
        viewModelScope.launch {
            while (true) {
                checkTimeBasedReminders()
                delay(4000) // check every 4 seconds
            }
        }
    }
    
    fun addTask(
        title: String, 
        category: String, 
        priority: Int,
        reminderTime: Long? = null,
        recurrence: String = "None",
        locationName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusInMeters: Float? = null
    ) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch
            val task = Task(
                title = title.trim(),
                category = category,
                priority = priority,
                isCompleted = false,
                reminderTime = reminderTime,
                recurrence = recurrence,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                radiusInMeters = radiusInMeters
            )
            repository.insert(task)
        }
    }
    
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val nextState = !task.isCompleted
            
            if (nextState) {
                // Play bell sound
                launch {
                    ChimeSoundGenerator.playBellChime()
                }
                
                // Trigger confetti burst
                _confettiTrigger.value += 1
                
                // Track completed timestamp
                val completedTimestamp = System.currentTimeMillis()

                if (task.recurrence != "None") {
                    // Create historical log clone of the task to preserve completed statistics
                    val historicalLog = task.copy(
                        id = 0, // Auto-generate new primary key
                        title = "${task.title} (Instance)",
                        isCompleted = true,
                        recurrence = "None", // Un-recur log
                        completedAt = completedTimestamp
                    )
                    repository.insert(historicalLog)

                    // Reschedule the recurring master task for the next reminder date
                    val nextTime = getNextRecurrenceTime(task.reminderTime ?: System.currentTimeMillis(), task.recurrence)
                    val updatedMaster = task.copy(
                        isCompleted = false,
                        hasTriggeredReminder = false,
                        reminderTime = nextTime,
                        createdAt = System.currentTimeMillis()
                    )
                    repository.update(updatedMaster)

                    val speakText = "Recurring task resolved. Automatically rescheduled next instance. " + encouragements.random()
                    ttsManager?.speak(speakText)
                } else {
                    // Standard task
                    val updatedTask = task.copy(
                        isCompleted = true,
                        completedAt = completedTimestamp
                    )
                    repository.update(updatedTask)

                    // Speak encouragement
                    val currentTasks = allTasks.value
                    val totalTasksCount = currentTasks.size
                    val completedCount = currentTasks.count { 
                        if (it.id == task.id) true else it.isCompleted 
                    }
                    
                    if (completedCount == totalTasksCount && totalTasksCount > 0) {
                        ttsManager?.speak("All tasks are finished. You have had an exceptionally productive day, sir!")
                    } else {
                        ttsManager?.speak(encouragements.random())
                    }
                }
            } else {
                // Uncomplete task
                val updatedTask = task.copy(isCompleted = false, completedAt = null)
                repository.update(updatedTask)
            }
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
    
    fun dismissTimeReminder() {
        _activeTimeReminder.value = null
    }

    fun dismissLocationReminder() {
        _activeLocationReminder.value = null
    }

    fun replayWelcomeVoice() {
        val speech = generateWelcomeSpeech(allTasks.value)
        ttsManager?.speak(speech)
    }

    fun speakProductivityReview() {
        val tasks = allTasks.value
        val completedCount = tasks.count { it.isCompleted }
        val highCount = tasks.count { it.isCompleted && it.priority == 2 }
        val streak = calculateDailyStreak(tasks)
        
        val summary = "Sir, here is your dynamic productivity report. " +
                "You have successfully locked in $completedCount tasks overall, including $highCount high priority critical goals. " +
                "Your daily consecutive streak is $streak days. " +
                if (streak >= 3) "Your consistency is outstanding. Excellent work."
                else "We are building excellent momentum. Let's aim to maintain this streak, sir."
                
        ttsManager?.speak(summary)
    }

    // Periodic check for scheduled time reminders
    private suspend fun checkTimeBasedReminders() {
        val now = System.currentTimeMillis()
        val currentTasks = allTasks.value
        for (task in currentTasks) {
            if (!task.isCompleted && !task.hasTriggeredReminder) {
                val remTime = task.reminderTime
                if (remTime != null && now >= remTime) {
                    // Update database so it doesn't trigger again
                    val updated = task.copy(hasTriggeredReminder = true)
                    repository.update(updated)
                    
                    // Show in active UI State
                    _activeTimeReminder.value = updated
                    
                    // Voice Alert
                    val alertText = "Excuse me, sir. Here is an active reminder: ${task.title}."
                    ttsManager?.speak(alertText)
                    
                    ChimeSoundGenerator.playBellChime()
                }
            }
        }
    }

    // Update location and evaluate geofence triggers
    fun updateUserLocation(name: String, lat: Double, lng: Double) {
        userLocationName.value = name
        userLatitude.value = lat
        userLongitude.value = lng
        
        viewModelScope.launch {
            val currentTasks = allTasks.value
            for (task in currentTasks) {
                if (!task.isCompleted && !task.hasTriggeredReminder) {
                    val taskLat = task.latitude
                    val taskLng = task.longitude
                    if (taskLat != null && taskLng != null) {
                        val distance = distanceInMeters(lat, lng, taskLat, taskLng)
                        val radius = task.radiusInMeters ?: 100f
                        if (distance <= radius) {
                            // Trigger location geofence!
                            val updated = task.copy(hasTriggeredReminder = true)
                            repository.update(updated)
                            
                            // Set alert state for UI
                            _activeLocationReminder.value = updated
                            
                            // Speak Alert
                            val loc = task.locationName ?: "your destination"
                            val alertText = "Attention, sir. You have arrived near $loc. Remember to: ${task.title}."
                            ttsManager?.speak(alertText)
                            
                            ChimeSoundGenerator.playBellChime()
                        }
                    }
                }
            }
        }
    }

    // Advanced recurrence datetime logic
    private fun getNextRecurrenceTime(currentTime: Long, recurrence: String): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        when (recurrence) {
            "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "Monthly" -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }

    // Calculate daily consecutive active streak
    fun calculateDailyStreak(tasks: List<Task>): Int {
        val completedDates = tasks.filter { it.isCompleted && it.completedAt != null }
            .map { 
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(it.completedAt!!))
            }
            .toSet()
            
        if (completedDates.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        while (true) {
            val dateStr = sdf.format(calendar.time)
            if (completedDates.contains(dateStr)) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                if (streak == 0) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStr = sdf.format(calendar.time)
                    if (completedDates.contains(yesterdayStr)) {
                        streak = 1
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    // Formulates butler welcome greetings based on time and performance metrics
    private fun generateWelcomeSpeech(tasks: List<Task>): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when {
            hour in 5..11 -> "Good morning, sir. I have synchronized your virtual agenda."
            hour in 12..17 -> "Good afternoon, sir. I hope your day is proceeding excellently."
            else -> "Good evening, sir. Let's manage your pending objectives."
        }
        
        val completedCount = tasks.count { it.isCompleted && it.completedAt != null }
        val activeStreak = calculateDailyStreak(tasks)
        
        val streakText = if (activeStreak > 0) {
            " You are currently maintaining an impressive $activeStreak-day productivity streak."
        } else " Let's kick off a new productivity streak today, sir."
        
        val statusText = if (tasks.none { !it.isCompleted }) {
            " All immediate tasks are completed. You have a clean slate."
        } else {
            val pendingCount = tasks.count { !it.isCompleted }
            " We currently have $pendingCount remaining directives requiring your attention."
        }
        
        return "$greeting$streakText$statusText"
    }

    // Haversine distance formula
    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (r * c).toFloat()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager?.shutdown()
    }
}
