package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) GlassBgSoft.copy(alpha = 0.04f) else GlassBgSoft,
        label = "card_bg"
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) GlassBorderSoft.copy(alpha = 0.05f) else GlassBorderSoft,
        label = "card_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (task.isCompleted) SlateTextMuted.copy(alpha = 0.5f) else SpaceWhite,
        label = "text_color"
    )
    val checkColor by animateColorAsState(
        targetValue = if (task.isCompleted) IndigoAccent else SpaceWhite.copy(alpha = 0.25f),
        label = "check_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_card_${task.id}")
            .clip(RoundedCornerShape(20.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox icon
            Icon(
                imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (task.isCompleted) "Completed" else "Uncompleted",
                tint = checkColor,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(18.dp))

            // Title and Metadata column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = textColor
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                // Wrapping row for tags
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Surface(
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
                    ) {
                        Text(
                            text = getCategoryEmoji(task.category) + " " + task.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Priority Badge
                    val (priorityText, priorityColor) = when (task.priority) {
                        2 -> "HIGH" to HotPink
                        1 -> "MED" to BrightYellow
                        else -> "LOW" to IndigoAccent
                    }

                    Surface(
                        color = priorityColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = priorityText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Optional Time Reminder Badge
                    if (task.reminderTime != null) {
                        val sdf = SimpleDateFormat("h:mm a, d MMM", Locale.getDefault())
                        val formattedTime = sdf.format(Date(task.reminderTime))
                        Surface(
                            color = IndigoAccent.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.NotificationsActive,
                                    contentDescription = "Time Reminder",
                                    tint = IndigoAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = IndigoAccent
                                )
                            }
                        }
                    }

                    // Optional Recurrence Badge
                    if (task.recurrence != "None") {
                        Surface(
                            color = VioletAccent.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, VioletAccent.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = "Recur cycle",
                                    tint = VioletAccent,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = task.recurrence.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = VioletAccent
                                )
                            }
                        }
                    }

                    // Optional Location Reminder Badge
                    if (task.latitude != null && task.longitude != null) {
                        val label = task.locationName ?: "Location"
                        Surface(
                            color = BrightYellow.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BrightYellow.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = "Location alert",
                                    tint = BrightYellow,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrightYellow
                                )
                            }
                        }
                    }
                }
            }

            // Deletion icon button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Task",
                    tint = HotPink.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "work" -> "💻"
        "tech" -> "💻"
        "fitness" -> "⚡"
        "lifestyle" -> "🌿"
        "learning" -> "📚"
        "general" -> "🔔"
        else -> "🔔"
    }
}
