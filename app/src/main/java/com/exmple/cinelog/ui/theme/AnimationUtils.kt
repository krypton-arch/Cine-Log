package com.exmple.cinelog.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ═════════════════════════════════════════════════════════
// Glassmorphism Design Tokens
// ═════════════════════════════════════════════════════════

/** Primary glass tint — warm noir with gold undertone */
val GlassTint = Color(0xFF1E1D1A)
/** Border luminance — subtle gold shimmer on glass edges */
val GlassBorderLight = Color(0xFFF5C518).copy(alpha = 0.08f)
/** Secondary border — pure white frost line */
val GlassBorderFrost = Color.White.copy(alpha = 0.06f)
/** Inner glow — warm ambient gold from within the glass */
val GlassInnerGlow = Color(0xFFF5C518).copy(alpha = 0.03f)

// ═════════════════════════════════════════════════════════
// Glassmorphism Modifiers
// ═════════════════════════════════════════════════════════

/**
 * Premium glassmorphic card — frosted translucent surface with
 * luminous gold border, inner glow gradient, and soft elevation.
 *
 * Use for: stat cards, info panels, feature cards.
 */
fun Modifier.glassCard(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.55f,
    borderAlpha: Float = 0.08f
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    GlassTint.copy(alpha = alpha),
                    GlassTint.copy(alpha = alpha * 0.7f)
                )
            ),
            shape = shape
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF5C518).copy(alpha = borderAlpha),
                    GlassBorderFrost,
                    Color(0xFFF5C518).copy(alpha = borderAlpha * 0.5f)
                )
            ),
            shape = shape
        )
        .drawBehind {
            // Inner glow — subtle radial warmth from top-left
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GlassInnerGlow,
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f, size.height * 0.15f),
                    radius = size.width * 0.8f
                ),
                cornerRadius = CornerRadius(cornerRadius.toPx())
            )
        }
}

/**
 * Subtle glass surface — lighter frosted pane for backgrounds
 * and input containers. Less prominent than glassCard.
 *
 * Use for: input fields, list item backgrounds, secondary panels.
 */
fun Modifier.glassSurface(
    cornerRadius: Dp = 12.dp,
    alpha: Float = 0.35f
): Modifier = composed {
    val shape = RoundedCornerShape(cornerRadius)
    this
        .clip(shape)
        .background(
            color = GlassTint.copy(alpha = alpha),
            shape = shape
        )
        .border(
            width = 0.5.dp,
            color = GlassBorderFrost,
            shape = shape
        )
}

/**
 * Regal gold-accented divider modifier. Draws a horizontal line
 * with a center fade-to-gold gradient.
 */
fun Modifier.regalDivider(): Modifier = this.drawBehind {
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFF5C518).copy(alpha = 0.15f),
                Color(0xFFF5C518).copy(alpha = 0.25f),
                Color(0xFFF5C518).copy(alpha = 0.15f),
                Color.Transparent
            )
        ),
        size = androidx.compose.ui.geometry.Size(size.width, 1.dp.toPx())
    )
}

// ═════════════════════════════════════════════════════════
// Interaction Animations
// ═════════════════════════════════════════════════════════

/**
 * Premium scale-down-on-press physical micro-interaction.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f,
    onClick: () -> Unit
) = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounceScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "bounceAlpha"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
}

// ═════════════════════════════════════════════════════════
// Loading States
// ═════════════════════════════════════════════════════════

/**
 * Premium animated shimmer skeleton brush for loading states.
 * Gold-tinted to match the Noir Archive theme.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E1D1A),
                Color(0xFF2A2718),
                Color(0xFF1E1D1A)
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

