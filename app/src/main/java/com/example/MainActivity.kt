package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.Task
import com.example.ui.TaskViewModel
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.DailyProgressHeader
import com.example.ui.components.TaskItemCard
import com.example.ui.components.VoiceAssistantBanner
import com.example.ui.theme.*
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainTodoScreen()
            }
        }
    }
}

@Composable
fun MainTodoScreen(
    viewModel: TaskViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val confettiTrigger by viewModel.confettiTrigger.collectAsStateWithLifecycle()
    
    // Alarms alerts
    val activeTimeReminder by viewModel.activeTimeReminder.collectAsStateWithLifecycle()
    val activeLocationReminder by viewModel.activeLocationReminder.collectAsStateWithLifecycle()

    // Location Trackers
    val userLocation by viewModel.userLocationName.collectAsStateWithLifecycle()
    val userLat by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLng by viewModel.userLongitude.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.count { !it.title.contains("(Instance)") } // count master tasks, not historical logs in basic count

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color.Transparent,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .size(60.dp)
                    .testTag("add_task_fab")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoAccent, VioletAccent)
                              )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Task",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(innerPadding)
        ) {
            // Background glows
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopStart)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                IndigoAccent.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            radius = 450f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.4f)
                    .align(Alignment.BottomEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                VioletAccent.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            radius = 500f
                        )
                    )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.012f),
                    radius = size.width * 0.45f,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Foreground Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp)
            ) {
                // Frosted Glass Custom Header
                item {
                    FrostedGlassHeader()
                }

                // Daily Progress Header Card
                item {
                    DailyProgressHeader(
                        completedCount = completedTasks,
                        totalCount = totalTasks.coerceAtLeast(completedTasks)
                    )
                }

                // AI Companion card
                item {
                    VoiceAssistantBanner(
                        onReplayVoice = { viewModel.replayWelcomeVoice() }
                    )
                }

                // Travel Simulation Control Panel (Inline)
                item {
                    TravelSimulatorCard(
                        userLocation = userLocation,
                        userLat = userLat,
                        userLng = userLng,
                        onTravel = { name, lat, lon -> viewModel.updateUserLocation(name, lat, lon) }
                    )
                }

                // Custom Productivity Activity Chart & Diagnostics Trigger
                item {
                    ProductivityInsightsCard(
                        tasks = tasks,
                        onVocalReport = { viewModel.speakProductivityReview() }
                    )
                }

                // Active Directives Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE TASKS DIRECTIVES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = IndigoAccent
                        )
                        
                        val activeCount = tasks.count { !it.isCompleted }
                        Text(
                            text = if (activeCount > 0) "$activeCount pending" else "Clear board",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextMuted
                        )
                    }
                }

                // Active Tasks items (excluding completed logs to keep board neat)
                val activeList = tasks.filter { !it.isCompleted }
                if (activeList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color.White.copy(alpha = 0.03f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = "Empty Clipboard",
                                    tint = SlateTextMuted.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your board is clear, sir.",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SpaceWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use the '+' controller below to program a new task or travel simulated coordinates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = activeList,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }

            // Confetti Overlay Layer
            ConfettiEffect(
                triggerCount = confettiTrigger,
                modifier = Modifier.matchParentSize()
            )

            // Alert Overlays
            AnimatedVisibility(
                visible = activeTimeReminder != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                activeTimeReminder?.let { task ->
                    ReminderAlertOverlay(
                        title = "🚨 ACTIVE TIMER ALARM",
                        taskTitle = task.title,
                        description = "This directive's scheduled alert threshold has been reached.",
                        iconColor = HotPink,
                        onDismiss = { viewModel.dismissTimeReminder() },
                        onResolve = {
                            viewModel.toggleTaskCompletion(task)
                            viewModel.dismissTimeReminder()
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = activeLocationReminder != null,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                activeLocationReminder?.let { task ->
                    ReminderAlertOverlay(
                        title = "📍 GEOFENCE PROXIMITY SENSORS",
                        taskTitle = task.title,
                        description = "You have crossed within range of: ${task.locationName ?: "Destination"}.",
                        iconColor = BrightYellow,
                        onDismiss = { viewModel.dismissLocationReminder() },
                        onResolve = {
                            viewModel.toggleTaskCompletion(task)
                            viewModel.dismissLocationReminder()
                        }
                    )
                }
            }
        }
    }

    // Modal dialog
    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = { showAddDialog = false },
            onAddTask = { title, category, priority, reminderTime, recurrence, locationName, latitude, longitude, radius ->
                viewModel.addTask(title, category, priority, reminderTime, recurrence, locationName, latitude, longitude, radius)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderAlertOverlay(
    title: String,
    taskTitle: String,
    description: String,
    iconColor: Color,
    onDismiss: () -> Unit,
    onResolve: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianBg.copy(alpha = 0.98f)),
            border = BorderStroke(2.dp, iconColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(iconColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (title.contains("TIMER")) Icons.Filled.NotificationsActive else Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = iconColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = taskTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, GlassBorderSoft),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SpaceWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("DISMISS")
                    }

                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = iconColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("RESOLVE DIRECTIVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TravelSimulatorCard(
    userLocation: String,
    userLat: Double,
    userLng: Double,
    onTravel: (name: String, lat: Double, lng: Double) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassBgSoft),
        border = BorderStroke(1.dp, GlassBorderSoft)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = BrightYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SPATIAL TRAVEL SIMULATOR",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = SlateTextMuted
                )
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Coordinates:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextMuted
                    )
                    Text(
                        text = String.format(Locale.US, "%.4f, %.4f", userLat, userLng),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = SpaceWhite
                    )
                }
                
                Surface(
                    color = BrightYellow.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BrightYellow.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = userLocation.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BrightYellow,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Simulate coordinates travel near destinations:",
                style = MaterialTheme.typography.labelSmall,
                color = SlateTextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val destinations = listOf(
                    Triple("Home 🏠", 37.7699, -122.4468),
                    Triple("Office 💼", 37.7891, -122.4014),
                    Triple("Grocery 🛒", 37.7749, -122.4194),
                    Triple("Reset 🗺️", 0.0, 0.0)
                )
                
                destinations.forEach { (label, lat, lon) ->
                    val isCurrent = userLocation == label.substringBefore(" ")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isCurrent) BrightYellow.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, if (isCurrent) BrightYellow else Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .clickable { onTravel(label.substringBefore(" "), lat, lon) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrent) BrightYellow else SpaceWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityInsightsCard(
    tasks: List<Task>,
    onVocalReport: () -> Unit
) {
    // Group tasks completed in last 7 days by day of week
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val completionCounts = IntArray(7) { 0 }
    
    val now = System.currentTimeMillis()
    val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
    
    val completedTasks = tasks.filter { it.isCompleted && it.completedAt != null && it.completedAt!! >= sevenDaysAgo }
    
    for (task in completedTasks) {
        val taskCal = Calendar.getInstance()
        taskCal.timeInMillis = task.completedAt!!
        val dayOfWeek = taskCal.get(Calendar.DAY_OF_WEEK) // 1 to 7
        completionCounts[dayOfWeek - 1]++
    }
    
    val maxCount = completionCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    
    // Calculate Streak
    val streak = remember(tasks) {
        // Simple computation helper
        var currentStreak = 0
        val completedDates = tasks.filter { it.isCompleted && it.completedAt != null }
            .map { 
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(it.completedAt!!))
            }
            .toSet()
            
        if (completedDates.isNotEmpty()) {
            val calendar = Calendar.getInstance()
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            while (true) {
                val dateStr = sdf.format(calendar.time)
                if (completedDates.contains(dateStr)) {
                    currentStreak++
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    if (currentStreak == 0) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterdayStr = sdf.format(calendar.time)
                        if (completedDates.contains(yesterdayStr)) {
                            currentStreak = 1
                            calendar.add(Calendar.DAY_OF_YEAR, -1)
                            continue
                        }
                    }
                    break
                }
            }
        }
        currentStreak
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = GlassBgSoft),
        border = BorderStroke(1.dp, GlassBorderSoft)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = VioletAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRODUCTIVITY ACTIVITY LOG",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = SlateTextMuted
                    )
                }
                
                Surface(
                    color = HotPink.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HotPink.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = "🔥 $streak DAY STREAK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = HotPink,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Draw beautiful custom bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Order of display starting 6 days ago until today
                val order = IntArray(7)
                val displayCal = Calendar.getInstance()
                displayCal.add(Calendar.DAY_OF_YEAR, -6)
                for (i in 0..6) {
                    order[i] = displayCal.get(Calendar.DAY_OF_WEEK) - 1
                    displayCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                for (idx in order) {
                    val count = completionCounts[idx]
                    val dayLabel = dayNames[idx]
                    val heightRatio = count.toFloat() / maxCount
                    val isToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 == idx
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(60.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(6.dp)
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(3.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(heightRatio.coerceAtLeast(0.08f))
                                    .width(6.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(VioletAccent, IndigoAccent)
                                        ),
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dayLabel.substring(0, 1),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isToday) IndigoAccent else SlateTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(14.dp))

            // Butler Voice Diagnostic Trigger button
            Button(
                onClick = onVocalReport,
                border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.3f)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.02f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = VioletAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REQUEST VOCAL DIAGNOSTIC REPORT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = SpaceWhite
                    )
                }
            }
        }
    }
}

@Composable
fun FrostedGlassHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "AI ASSISTANT MONITORS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = IndigoAccent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Welcome, Sir",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White
            )
        }
        EqualizerWave()
    }
}

@Composable
fun EqualizerWave() {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")
    
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h5"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.height(16.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(Modifier.width(2.5.dp).fillMaxHeight(h1).background(IndigoAccent, RoundedCornerShape(1.dp)))
            Box(Modifier.width(2.5.dp).fillMaxHeight(h2).background(IndigoAccent, RoundedCornerShape(1.dp)))
            Box(Modifier.width(2.5.dp).fillMaxHeight(h3).background(IndigoAccent, RoundedCornerShape(1.dp)))
            Box(Modifier.width(2.5.dp).fillMaxHeight(h4).background(IndigoAccent, RoundedCornerShape(1.dp)))
            Box(Modifier.width(2.5.dp).fillMaxHeight(h5).background(IndigoAccent, RoundedCornerShape(1.dp)))
        }
    }
}
