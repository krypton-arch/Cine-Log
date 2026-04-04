package com.exmple.cinelog.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun NoirBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF111111))
            .drawWithCache {
                val random = Random(17)
                val particleCount = ((size.width * size.height) / 1400f)
                    .toInt()
                    .coerceIn(180, 520)

                val particles = List(particleCount) {
                    Triple(
                        Offset(
                            x = random.nextFloat() * size.width,
                            y = random.nextFloat() * size.height
                        ),
                        0.2f + random.nextFloat() * 1.1f,
                        0.015f + random.nextFloat() * 0.045f
                    )
                }

                onDrawWithContent {
                    drawContent()
                    particles.forEach { (center, radius, alpha) ->
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = radius,
                            center = center
                        )
                    }
                }
            }
    )
}
