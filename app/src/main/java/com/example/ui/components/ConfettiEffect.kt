package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Random

data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    var alpha: Float = 1f,
    val isCircle: Boolean = false
)

@Composable
fun ConfettiEffect(
    triggerCount: Int,
    modifier: Modifier = Modifier
) {
    if (triggerCount == 0) return

    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val random = remember { Random() }
    var tick by remember { mutableStateOf(0) }

    // When triggerCount changes, spawn a burst of confetti!
    LaunchedEffect(triggerCount) {
        if (triggerCount > 0) {
            val list = mutableListOf<ConfettiParticle>()
            val colors = listOf(
                Color(0xFF66FCF1), // Cyber Cyan / Teal
                Color(0xFF8A2BE2), // Cyber Violet
                Color(0xFFFF007F), // Neon Magenta
                Color(0xFFFFD700), // Gold
                Color(0xFF00FF7F), // Spring Green
                Color(0xFF00BFFF)  // Deep Sky Blue
            )
            // Spawn 120 particles bursting up and out from center
            for (i in 0..120) {
                list.add(
                    ConfettiParticle(
                        x = -1f, // initialized to canvas center dynamically
                        y = -1f,
                        vx = (random.nextFloat() - 0.5f) * 45f,
                        vy = -20f - random.nextFloat() * 35f,
                        color = colors[random.nextInt(colors.size)],
                        size = 12f + random.nextFloat() * 18f,
                        rotation = random.nextFloat() * 360f,
                        rotationSpeed = (random.nextFloat() - 0.5f) * 20f,
                        isCircle = random.nextBoolean()
                    )
                )
            }
            particles.addAll(list)
        }
    }

    // Frame update loop (60 FPS)
    LaunchedEffect(particles.size) {
        if (particles.isEmpty()) return@LaunchedEffect
        val gravity = 0.7f
        val drag = 0.96f
        
        while (isActive && particles.isNotEmpty()) {
            for (i in particles.indices) {
                if (i < particles.size) {
                    val p = particles[i]
                    p.x += p.vx
                    p.y += p.vy
                    p.vy += gravity
                    p.vx *= drag
                    p.vy *= drag
                    p.rotation += p.rotationSpeed
                    p.alpha -= 0.016f // fade over 1.5 seconds approx
                    if (p.alpha < 0) p.alpha = 0f
                }
            }
            
            // Remove faded particles
            val alive = particles.filter { it.alpha > 0.01f }
            particles.clear()
            particles.addAll(alive)
            
            // Trigger recomposition
            tick++
            delay(16) // ~60fps
        }
    }

    // Reference tick to trigger recomposition of Canvas
    key(tick) {
        if (particles.isNotEmpty()) {
            Canvas(modifier = modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                particles.forEach { p ->
                    if (p.x == -1f || p.y == -1f) {
                        p.x = canvasWidth / 2f
                        p.y = canvasHeight * 0.65f // Burst from lower-middle center
                    }

                    val colorWithAlpha = p.color.copy(alpha = p.alpha)
                    rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                        if (p.isCircle) {
                            drawCircle(
                                color = colorWithAlpha,
                                radius = p.size / 2,
                                center = Offset(p.x, p.y)
                            )
                        } else {
                            drawRect(
                                color = colorWithAlpha,
                                topLeft = Offset(p.x - p.size / 2, p.y - p.size / 2),
                                size = Size(p.size, p.size * 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
