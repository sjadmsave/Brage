package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.engine.BoardGeometry
import com.example.model.BoardHole
import com.example.model.BoardTheme
import com.example.model.Plank
import com.example.model.PlankStatus
import com.example.ui.theme.BrassBright
import com.example.ui.theme.BrassGold
import com.example.ui.theme.CopperAccent
import com.example.ui.theme.CrimsonBlocked
import com.example.ui.theme.EmeraldValid
import com.example.ui.theme.SkyHint
import com.example.viewmodel.GameUiState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun WoodBoardCanvas(
    uiState: GameUiState,
    onBoardTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "board_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val holeById = uiState.holes.associateBy { it.id }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_board_canvas")
            .pointerInput(uiState.holes, uiState.bolts, uiState.selectedBoltId, uiState.isWon) {
                detectTapGestures { tapOffset ->
                    val scale = min(
                        size.width / BoardGeometry.BOARD_WIDTH,
                        size.height / BoardGeometry.BOARD_HEIGHT
                    )
                    val offsetX = (size.width - BoardGeometry.BOARD_WIDTH * scale) * 0.5f
                    val offsetY = (size.height - BoardGeometry.BOARD_HEIGHT * scale) * 0.5f
                    val bx = (tapOffset.x - offsetX) / scale
                    val by = (tapOffset.y - offsetY) / scale
                    if (bx in -16f..(BoardGeometry.BOARD_WIDTH + 16f) &&
                        by in -16f..(BoardGeometry.BOARD_HEIGHT + 16f)
                    ) {
                        onBoardTap(bx, by)
                    }
                }
            }
    ) {
        val scale = min(
            size.width / BoardGeometry.BOARD_WIDTH,
            size.height / BoardGeometry.BOARD_HEIGHT
        )
        val offsetX = (size.width - BoardGeometry.BOARD_WIDTH * scale) * 0.5f
        val offsetY = (size.height - BoardGeometry.BOARD_HEIGHT * scale) * 0.5f

        withTransform({
            translate(left = offsetX, top = offsetY)
            scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
        }) {
            // 1. Cyber-Tech Chassis & Blueprint Grid
            drawCyberChassisFrame(uiState.boardTheme)

            // 2. Laser-cut Socket Ports
            for (hole in uiState.holes) {
                drawTechSocket(
                    hole = hole,
                    isAvailable = hole.id in uiState.availableHoleIds,
                    isBlockedFlash = hole.id == uiState.blockedFlashHoleId,
                    isHintTarget = uiState.hintMove?.targetHoleId == hole.id,
                    pulse = pulse
                )
            }

            // 3. Tech Composite Panels sorted by layer
            val sortedPlanks = uiState.planks
                .filter { it.status != PlankStatus.CLEARED }
                .sortedWith(compareBy<Plank> { it.status == PlankStatus.FALLING }.thenBy { it.layer })

            for (plank in sortedPlanks) {
                drawTechPanel(plank)
            }

            // 4. Target Reticle Overlay for available aligned sockets
            if (uiState.selectedBoltId != null) {
                for (hole in uiState.holes) {
                    if (hole.id in uiState.availableHoleIds) {
                        drawAvailableTargetReticle(
                            hole = hole,
                            isHintTarget = uiState.hintMove?.targetHoleId == hole.id,
                            pulse = pulse
                        )
                    }
                }
            }

            // 5. Blocked alert ring
            uiState.blockedFlashHoleId?.let { flashId ->
                holeById[flashId]?.let { h ->
                    drawCircle(
                        color = CrimsonBlocked.copy(alpha = 0.9f),
                        radius = h.radius + 6f,
                        center = Offset(h.x, h.y),
                        style = Stroke(width = 3.5f)
                    )
                }
            }

            // 6. Machined Titanium & Neon Bolts
            for (bolt in uiState.bolts) {
                val hole = holeById[bolt.holeId] ?: continue
                val isSelected = bolt.id == uiState.selectedBoltId
                val isHintSource = uiState.hintMove?.boltId == bolt.id
                drawTechBolt(
                    center = Offset(hole.x, hole.y),
                    isSelected = isSelected,
                    isHintSource = isHintSource,
                    isReinforced = bolt.reinforced,
                    pulse = pulse
                )
            }

            // 7. Holographic Hint Vector Arrow
            val hint = uiState.hintMove
            if (hint != null) {
                val srcBolt = uiState.bolts.firstOrNull { it.id == hint.boltId }
                val srcHole = srcBolt?.let { holeById[it.holeId] }
                val dstHole = holeById[hint.targetHoleId]
                if (srcHole != null && dstHole != null) {
                    drawHintArrow(
                        from = Offset(srcHole.x, srcHole.y),
                        to = Offset(dstHole.x, dstHole.y),
                        pulse = pulse
                    )
                }
            }

            // 8. Digital Neon Sparks & Victory Particles
            for (p in uiState.particles) {
                val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                rotate(degrees = p.rotation * 57.3f, pivot = Offset(p.x, p.y)) {
                    drawRoundRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(p.x - p.size * 0.5f, p.y - p.size * 0.5f),
                        size = Size(p.size, p.size * 0.7f),
                        cornerRadius = CornerRadius(1.5f, 1.5f)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCyberChassisFrame(theme: BoardTheme) {
    val w = BoardGeometry.BOARD_WIDTH
    val h = BoardGeometry.BOARD_HEIGHT
    val accent = theme.accentColor

    // Outer ambient glow shadow
    drawRoundRect(
        color = accent.copy(alpha = 0.09f),
        topLeft = Offset(-2f, -2f),
        size = Size(w + 4f, h + 4f),
        cornerRadius = CornerRadius(24f, 24f)
    )

    // Brushed titanium chassis border
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(theme.frameTopColor, theme.frameBottomColor, theme.frameTopColor.copy(alpha = 0.85f)),
            start = Offset.Zero,
            end = Offset(w, h)
        ),
        topLeft = Offset.Zero,
        size = Size(w, h),
        cornerRadius = CornerRadius(22f, 22f)
    )

    // Inner obsidian circuit deck
    val inset = 10f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(theme.deckTopColor, theme.frameBottomColor.copy(alpha = 0.45f), theme.deckBottomColor)
        ),
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2f, h - inset * 2f),
        cornerRadius = CornerRadius(16f, 16f)
    )

    // Subtle CAD blueprint grid lines
    val gridStep = 36f
    var gx = inset + gridStep
    while (gx < w - inset) {
        drawLine(
            color = accent.copy(alpha = 0.08f),
            start = Offset(gx, inset + 4f),
            end = Offset(gx, h - inset - 4f),
            strokeWidth = 1f
        )
        gx += gridStep
    }
    var gy = inset + gridStep
    while (gy < h - inset) {
        drawLine(
            color = accent.copy(alpha = 0.08f),
            start = Offset(inset + 4f, gy),
            end = Offset(w - inset - 4f, gy),
            strokeWidth = 1f
        )
        gy += gridStep
    }

    // Glowing border trace
    drawRoundRect(
        color = accent.copy(alpha = 0.48f),
        topLeft = Offset(inset, inset),
        size = Size(w - inset * 2f, h - inset * 2f),
        cornerRadius = CornerRadius(16f, 16f),
        style = Stroke(width = 1.4f)
    )

    // 4 Corner neon sensor brackets
    val cornerLen = 18f
    val m = 5f
    val bracketColor = accent.copy(alpha = 0.90f)
    drawLine(bracketColor, Offset(m, m + cornerLen), Offset(m, m + 6f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(m + 6f, m), Offset(m + cornerLen, m), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(w - m, m + cornerLen), Offset(w - m, m + 6f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(w - m - 6f, m), Offset(w - m - cornerLen, m), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(m, h - m - cornerLen), Offset(m, h - m - 6f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(m + 6f, h - m), Offset(m + cornerLen, h - m), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(w - m, h - m - cornerLen), Offset(w - m, h - m - 6f), strokeWidth = 2.2f, cap = StrokeCap.Round)
    drawLine(bracketColor, Offset(w - m - 6f, h - m), Offset(w - m - cornerLen, h - m), strokeWidth = 2.2f, cap = StrokeCap.Round)
}

private fun DrawScope.drawTechSocket(
    hole: BoardHole,
    isAvailable: Boolean,
    isBlockedFlash: Boolean,
    isHintTarget: Boolean,
    pulse: Float
) {
    val c = Offset(hole.x, hole.y)
    // Anodized socket collar
    drawCircle(
        color = Color(0xFF1F3356),
        radius = hole.radius + 2.5f,
        center = c
    )
    // Deep recessed port
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF02050C), Color(0xFF091224)),
            center = c,
            radius = hole.radius
        ),
        radius = hole.radius,
        center = c
    )
    // Subtle cyan precision ring
    drawCircle(
        color = BrassGold.copy(alpha = 0.28f),
        radius = hole.radius - 2f,
        center = c,
        style = Stroke(width = 1.2f)
    )

    if (isAvailable) {
        val ringColor = if (isHintTarget) SkyHint else EmeraldValid
        drawCircle(
            color = ringColor.copy(alpha = 0.26f + pulse * 0.24f),
            radius = hole.radius + 3f + pulse * 2.5f,
            center = c
        )
    }
    if (isBlockedFlash) {
        drawCircle(
            color = CrimsonBlocked.copy(alpha = 0.45f),
            radius = hole.radius + 4f,
            center = c
        )
    }
}

private fun DrawScope.drawAvailableTargetReticle(
    hole: BoardHole,
    isHintTarget: Boolean,
    pulse: Float
) {
    val c = Offset(hole.x, hole.y)
    val color = if (isHintTarget) SkyHint else EmeraldValid
    val r = hole.radius + 2f + pulse * 2.2f
    drawCircle(
        color = color.copy(alpha = 0.78f + pulse * 0.22f),
        radius = r,
        center = c,
        style = Stroke(width = if (isHintTarget) 3.2f else 2.5f)
    )
    // 4 Precision target crosshair ticks
    val tickInner = r + 1f
    val tickOuter = r + 5f
    drawLine(color, c + Offset(tickInner, 0f), c + Offset(tickOuter, 0f), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(color, c - Offset(tickInner, 0f), c - Offset(tickOuter, 0f), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(color, c + Offset(0f, tickInner), c + Offset(0f, tickOuter), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(color, c - Offset(0f, tickInner), c - Offset(0f, tickOuter), strokeWidth = 2f, cap = StrokeCap.Round)
}

private fun DrawScope.drawTechPanel(plank: Plank) {
    val alpha = plank.alpha.coerceIn(0f, 1f)
    if (alpha <= 0.01f) return

    val angleDeg = plank.angleRad * 180f / PI.toFloat()
    val corner = min(plank.height * 0.36f, 11f)
    val shadowShift = 3.5f + plank.layer * 1.4f

    withTransform({
        translate(left = plank.centerX, top = plank.centerY)
        rotate(degrees = angleDeg, pivot = Offset.Zero)
    }) {
        val halfW = plank.width * 0.5f
        val halfH = plank.height * 0.5f
        val topLeft = Offset(-halfW, -halfH)
        val rectSize = Size(plank.width, plank.height)

        // Drop shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.52f * alpha),
            topLeft = Offset(-halfW + shadowShift * 0.6f, -halfH + shadowShift),
            size = rectSize,
            cornerRadius = CornerRadius(corner, corner)
        )

        // Anodized alloy panel body gradient
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    plank.woodStyle.lightColor.copy(alpha = alpha * 0.96f),
                    plank.woodStyle.darkColor.copy(alpha = alpha * 0.98f)
                ),
                startY = -halfH,
                endY = halfH
            ),
            topLeft = topLeft,
            size = rectSize,
            cornerRadius = CornerRadius(corner, corner)
        )

        // Digital PCB / Circuit trace lines running between sockets
        val insetX = corner * 1.2f
        drawLine(
            color = plank.woodStyle.grainColor.copy(alpha = 0.38f * alpha),
            start = Offset(-halfW + insetX, -halfH * 0.42f),
            end = Offset(halfW - insetX, -halfH * 0.42f),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = plank.woodStyle.grainColor.copy(alpha = 0.30f * alpha),
            start = Offset(-halfW + insetX, halfH * 0.42f),
            end = Offset(halfW - insetX, halfH * 0.42f),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )

        // Crisp neon-tinted metallic edge highlight
        drawRoundRect(
            color = plank.woodStyle.highlightColor.copy(alpha = 0.65f * alpha),
            topLeft = topLeft + Offset(1f, 1f),
            size = Size(plank.width - 2f, plank.height - 2f),
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.5f)
        )
        drawRoundRect(
            color = Color(0xFF040812).copy(alpha = 0.55f * alpha),
            topLeft = topLeft,
            size = rectSize,
            cornerRadius = CornerRadius(corner, corner),
            style = Stroke(width = 1.2f)
        )

        // Precision machined panel socket holes
        for (lh in plank.localHoles) {
            val hc = Offset(lh.x, lh.y)
            drawCircle(
                color = plank.woodStyle.darkColor.copy(alpha = alpha),
                radius = 12.5f,
                center = hc
            )
            drawCircle(
                color = Color(0xFF050912).copy(alpha = 0.95f * alpha),
                radius = 10f,
                center = hc
            )
            drawCircle(
                color = plank.woodStyle.highlightColor.copy(alpha = 0.55f * alpha),
                radius = 10.5f,
                center = hc,
                style = Stroke(width = 1.3f)
            )
        }
    }
}

private fun DrawScope.drawTechBolt(
    center: Offset,
    isSelected: Boolean,
    isHintSource: Boolean,
    isReinforced: Boolean,
    pulse: Float
) {
    val scale = if (isSelected) 1.22f + pulse * 0.05f else 1.0f
    val liftY = if (isSelected) -3.5f else 0f
    val c = center + Offset(0f, liftY)
    val radius = 12.5f * scale

    // Protective energy shield ring on reinforced bolts
    if (isReinforced) {
        drawCircle(
            color = CrimsonBlocked.copy(alpha = 0.24f + pulse * 0.16f),
            radius = radius + 6.5f,
            center = c
        )
        drawCircle(
            color = CrimsonBlocked,
            radius = radius + 5.5f,
            center = c,
            style = Stroke(
                width = 2.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), phase = pulse * 10f)
            )
        )
    }

    // Selection or hint holographic ring
    if (isSelected) {
        drawCircle(
            color = BrassBright.copy(alpha = 0.30f + pulse * 0.25f),
            radius = radius + 8f,
            center = c
        )
        drawCircle(
            color = BrassBright,
            radius = radius + 4.5f,
            center = c,
            style = Stroke(width = 2.5f)
        )
    } else if (isHintSource) {
        drawCircle(
            color = SkyHint.copy(alpha = 0.45f + pulse * 0.35f),
            radius = radius + 6.5f,
            center = c,
            style = Stroke(width = 2.8f)
        )
    }

    // Shadow under bolt
    drawCircle(
        color = Color.Black.copy(alpha = if (isSelected) 0.65f else 0.48f),
        radius = radius,
        center = center + Offset(1.2f, if (isSelected) 4.5f else 2.2f)
    )

    // Brushed titanium outer collar
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFE4EEFF), Color(0xFF5D7294)),
            center = c - Offset(radius * 0.3f, radius * 0.3f),
            radius = radius * 1.4f
        ),
        radius = radius,
        center = c
    )

    // Glowing neon-amber / cyan-rimmed hex head
    val hexRadius = radius * 0.78f
    val hexPath = Path().apply {
        for (i in 0 until 6) {
            val a = (i * 60.0 - 30.0) * PI / 180.0
            val px = c.x + (hexRadius * cos(a)).toFloat()
            val py = c.y + (hexRadius * sin(a)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(
        path = hexPath,
        brush = Brush.linearGradient(
            colors = if (isSelected) {
                listOf(BrassBright, BrassGold, Color(0xFF007799))
            } else {
                listOf(Color(0xFFFFD166), CopperAccent, Color(0xFF9C5E08))
            },
            start = c - Offset(hexRadius, hexRadius),
            end = c + Offset(hexRadius, hexRadius)
        )
    )
    drawPath(
        path = hexPath,
        color = Color(0xFF0B162C).copy(alpha = 0.75f),
        style = Stroke(width = 1.1f)
    )

    // Glowing core & precision cross-drive
    drawCircle(
        color = if (isSelected) BrassBright else Color(0xFF1B2438),
        radius = radius * 0.30f,
        center = c
    )
    drawLine(
        color = if (isSelected) Color(0xFF04101F) else BrassBright.copy(alpha = 0.85f),
        start = c + Offset(-radius * 0.18f, 0f),
        end = c + Offset(radius * 0.18f, 0f),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = if (isSelected) Color(0xFF04101F) else BrassBright.copy(alpha = 0.85f),
        start = c + Offset(0f, -radius * 0.18f),
        end = c + Offset(0f, radius * 0.18f),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawHintArrow(from: Offset, to: Offset, pulse: Float) {
    val angle = atan2(to.y - from.y, to.x - from.x)
    val start = from + Offset(cos(angle) * 18f, sin(angle) * 18f)
    val end = to - Offset(cos(angle) * 18f, sin(angle) * 18f)

    drawLine(
        color = SkyHint.copy(alpha = 0.80f + pulse * 0.20f),
        start = start,
        end = end,
        strokeWidth = 3.5f,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), phase = -pulse * 18f)
    )

    val headLen = 11f
    val leftAngle = angle + PI.toFloat() * 0.8f
    val rightAngle = angle - PI.toFloat() * 0.8f
    drawLine(
        color = SkyHint,
        start = end,
        end = end + Offset(cos(leftAngle) * headLen, sin(leftAngle) * headLen),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = SkyHint,
        start = end,
        end = end + Offset(cos(rightAngle) * headLen, sin(rightAngle) * headLen),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
}
