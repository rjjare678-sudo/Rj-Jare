package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (
        title: String, 
        category: String, 
        priority: Int,
        reminderTime: Long?,
        recurrence: String,
        locationName: String?,
        latitude: Double?,
        longitude: Double?,
        radiusInMeters: Float?
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var selectedPriority by remember { mutableStateOf(1) } // Default Medium

    // Advanced Reminders State
    var enableTimeReminder by remember { mutableStateOf(false) }
    var reminderTimeMs by remember { mutableStateOf(System.currentTimeMillis() + 5 * 60 * 1000) } // Default +5 min
    var selectedRecurrence by remember { mutableStateOf("None") }

    // Location Reminders State
    var enableLocationReminder by remember { mutableStateOf(false) }
    var locationNameInput by remember { mutableStateOf("Grocery Store") }
    var latitudeInput by remember { mutableStateOf("37.7749") }
    var longitudeInput by remember { mutableStateOf("-122.4194") }
    var radiusInput by remember { mutableStateOf("100") }

    val categories = listOf("General", "Work", "Fitness", "Lifestyle", "Learning")
    val recurrences = listOf("None", "Daily", "Weekly", "Monthly")
    
    val categoryScrollState = rememberScrollState()
    val mainScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = GlassBgSoft.copy(alpha = 0.95f)),
            border = BorderStroke(1.dp, GlassBorderSoft),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(mainScrollState)
                    .padding(24.dp)
            ) {
                Text(
                    text = "CREATE NEW DIRECTIVE",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp),
                    color = IndigoAccent
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Title input field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs to be done, sir?", color = SlateTextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoAccent,
                        unfocusedBorderColor = GlassBorderSoft,
                        focusedLabelColor = IndigoAccent,
                        unfocusedLabelColor = SlateTextMuted,
                        focusedTextColor = SpaceWhite,
                        unfocusedTextColor = SpaceWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_input_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Category selector label
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = SlateTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable category pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(categoryScrollState)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            color = if (isSelected) IndigoAccent else Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) IndigoAccent else Color.White.copy(alpha = 0.04f)),
                            modifier = Modifier
                                .clickable { selectedCategory = category }
                                .testTag("category_pill_$category")
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.Black else SpaceWhite,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority selector label
                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = SlateTextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Priority segmented options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 1, 2).forEach { priority ->
                        val isSelected = priority == selectedPriority
                        val priorityLabel = when (priority) {
                            2 -> "High 🔴"
                            1 -> "Med 🟡"
                            else -> "Low 🟢"
                        }
                        
                        val activeColor = when (priority) {
                            2 -> HotPink
                            1 -> BrightYellow
                            else -> IndigoAccent
                        }

                        Surface(
                            color = if (isSelected) activeColor else Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSelected) activeColor else Color.White.copy(alpha = 0.04f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPriority = priority }
                                .testTag("priority_pill_$priority")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = priorityLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) Color.Black else SpaceWhite,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // 1. ADVANCED TIME-BASED REMINDERS TABS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = IndigoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Time-Based Reminder",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SpaceWhite
                            )
                            Text(
                                text = "Schedule notifications & alerts",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextMuted
                            )
                        }
                    }
                    Switch(
                        checked = enableTimeReminder,
                        onCheckedChange = { enableTimeReminder = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = IndigoAccent,
                            checkedTrackColor = IndigoAccent.copy(alpha = 0.3f),
                            uncheckedThumbColor = SlateTextMuted,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                }

                if (enableTimeReminder) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GlassBorderSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val sdf = SimpleDateFormat("EEE, d MMM yyyy, HH:mm", Locale.getDefault())
                            val formattedTime = sdf.format(Date(reminderTimeMs))

                            Text(
                                text = "Selected Alarm Time:",
                                style = MaterialTheme.typography.labelMedium,
                                color = SlateTextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = SpaceWhite
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Quick Offset Presets:",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Offset Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    "+1 Min" to 1 * 60 * 1000L,
                                    "+5 Min" to 5 * 60 * 1000L,
                                    "+1 Hour" to 60 * 60 * 1000L,
                                    "Tomorrow" to 24 * 60 * 60 * 1000L
                                )
                                presets.forEach { (label, duration) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .clickable { reminderTimeMs = System.currentTimeMillis() + duration }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = IndigoAccent
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // RECURRENCE OPTIONS
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Repeat, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recurrence Period",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SlateTextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                recurrences.forEach { recurrence ->
                                    val isRecurSelected = recurrence == selectedRecurrence
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isRecurSelected) VioletAccent else Color.White.copy(alpha = 0.04f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedRecurrence = recurrence }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = recurrence,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isRecurSelected) Color.Black else SpaceWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // 2. LOCATION-BASED REMINDERS TABS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = VioletAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Location Geofence Alert",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SpaceWhite
                            )
                            Text(
                                text = "Triggers when entering spatial radius",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextMuted
                            )
                        }
                    }
                    Switch(
                        checked = enableLocationReminder,
                        onCheckedChange = { enableLocationReminder = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VioletAccent,
                            checkedTrackColor = VioletAccent.copy(alpha = 0.3f),
                            uncheckedThumbColor = SlateTextMuted,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                }

                if (enableLocationReminder) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GlassBorderSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Preset location buttons that directly match travel simulation
                            Text(
                                text = "Target Preset Destinations:",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val locationPresets = listOf(
                                Triple("Home 🏠", "37.7699", "-122.4468"),
                                Triple("Office 💼", "37.7891", "-122.4014"),
                                Triple("Grocery 🛒", "37.7749", "-122.4194")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                locationPresets.forEach { (label, lat, lon) ->
                                    val isCurrentPreset = latitudeInput == lat && longitudeInput == lon
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isCurrentPreset) IndigoAccent else Color.White.copy(alpha = 0.04f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                locationNameInput = label.substringBefore(" ")
                                                latitudeInput = lat
                                                longitudeInput = lon
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCurrentPreset) Color.Black else SpaceWhite
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Name field
                            OutlinedTextField(
                                value = locationNameInput,
                                onValueChange = { locationNameInput = it },
                                label = { Text("Destination Name (e.g. Grocery Store)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletAccent,
                                    unfocusedBorderColor = GlassBorderSoft,
                                    focusedTextColor = SpaceWhite,
                                    unfocusedTextColor = SpaceWhite
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Lat Long Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = latitudeInput,
                                    onValueChange = { latitudeInput = it },
                                    label = { Text("Latitude") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VioletAccent,
                                        unfocusedBorderColor = GlassBorderSoft,
                                        focusedTextColor = SpaceWhite,
                                        unfocusedTextColor = SpaceWhite
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = longitudeInput,
                                    onValueChange = { longitudeInput = it },
                                    label = { Text("Longitude") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VioletAccent,
                                        unfocusedBorderColor = GlassBorderSoft,
                                        focusedTextColor = SpaceWhite,
                                        unfocusedTextColor = SpaceWhite
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Radius Field
                            OutlinedTextField(
                                value = radiusInput,
                                onValueChange = { radiusInput = it },
                                label = { Text("Geofence Radius (meters)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = VioletAccent,
                                    unfocusedBorderColor = GlassBorderSoft,
                                    focusedTextColor = SpaceWhite,
                                    unfocusedTextColor = SpaceWhite
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = SlateTextMuted)
                    ) {
                        Text("CANCEL")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Styled Indigo/Violet premium button
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val rTime = if (enableTimeReminder) reminderTimeMs else null
                                val recurStr = if (enableTimeReminder) selectedRecurrence else "None"
                                val locName = if (enableLocationReminder) locationNameInput else null
                                val lat = if (enableLocationReminder) latitudeInput.toDoubleOrNull() else null
                                val lon = if (enableLocationReminder) longitudeInput.toDoubleOrNull() else null
                                val radius = if (enableLocationReminder) radiusInput.toFloatOrNull() ?: 100f else null
                                
                                onAddTask(title, selectedCategory, selectedPriority, rTime, recurStr, locName, lat, lon, radius)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("submit_button")
                            .height(48.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(IndigoAccent, VioletAccent)
                                    )
                                )
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CREATE DIRECTIVE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
