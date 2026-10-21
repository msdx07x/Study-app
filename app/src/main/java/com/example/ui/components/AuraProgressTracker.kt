package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCardBorder
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraEmeraldBright
import com.example.ui.theme.AuraGlowBorder
import com.example.ui.theme.AuraGlowCardBrush
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraPink
import com.example.ui.theme.AuraPurple
import com.example.ui.theme.AuraSurface
import com.example.ui.theme.AuraSurfaceVariant
import com.example.ui.theme.AuraViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AuraProgressTrackerCard(
    totalChapters: Int,
    completedChapters: Int,
    dailyGoal: Int = 2,
    todayCompleted: Int = 0,
    modifier: Modifier = Modifier,
    onViewDetailsClick: (() -> Unit)? = null
) {
    val progress = if (totalChapters > 0) {
        (completedChapters.toFloat() / totalChapters.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(progress, animationSpec = tween(durationMillis = 900))
    }

    val percentage = (progress * 100).toInt()

    Card(
        modifier = modifier
            .testTag("aura_progress_tracker_card")
            .fillMaxWidth()
            .border(1.dp, AuraGlowBorder, RoundedCornerShape(20.dp))
            .clickable(enabled = onViewDetailsClick != null) { onViewDetailsClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = AuraSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraGlowCardBrush)
                .drawBehind {
                    // Soft glowing aura ambient light in the top-right
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AuraPurple.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.2f),
                            radius = size.width * 0.45f
                        )
                    )
                    // Secondary cyan glow in bottom-left
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(AuraCyan.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.85f),
                            radius = size.width * 0.35f
                        )
                    )
                }
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: Aura Badge & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(AuraPurple, AuraCyan))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Study Progress Tracker",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Aura Mastery Engine",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AuraCyan,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    // Streak Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraSurfaceVariant)
                            .border(1.dp, AuraAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AuraAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (completedChapters > 0) "Active Streak" else "Start Today",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Middle Row: Circular Glowing Aura Ring + Key Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Circular Aura Dial
                    Box(
                        modifier = Modifier.size(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val strokeWidth = 9.dp.toPx()
                            val arcSize = size.width - strokeWidth
                            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                            // Track circle
                            drawArc(
                                color = AuraCardBorder,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Glowing progress arc
                            if (animatedProgress.value > 0f) {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(AuraPurple, AuraIndigo, AuraCyan, AuraEmeraldBright, AuraPurple)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress.value,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    fontSize = 22.sp
                                )
                            )
                            Text(
                                text = "COMPLETE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AuraCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Metrics Column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric 1: Completed vs Total
                        AuraMetricRow(
                            label = "Mastered",
                            value = "$completedChapters of $totalChapters",
                            color = AuraCyan,
                            icon = Icons.Default.CheckCircle
                        )

                        // Metric 2: Pending
                        val pending = (totalChapters - completedChapters).coerceAtLeast(0)
                        AuraMetricRow(
                            label = "In Progress",
                            value = "$pending remaining",
                            color = AuraPurple,
                            icon = Icons.Default.Schedule
                        )

                        // Metric 3: Focus Status
                        AuraMetricRow(
                            label = "Study Status",
                            value = when {
                                totalChapters == 0 -> "Import a course"
                                percentage >= 100 -> "Course Mastered!"
                                percentage >= 50 -> "Over halfway!"
                                else -> "Building momentum"
                            },
                            color = AuraEmeraldBright,
                            icon = Icons.Default.AutoAwesome
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Weekly Momentum Pills (Mon - Sun)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val currentDayIndex = 2 // Mid-week anchor
                    days.forEachIndexed { idx, day ->
                        val isDone = idx <= currentDayIndex && completedChapters > 0
                        val isToday = idx == currentDayIndex

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDone -> Brush.linearGradient(listOf(AuraPurple, AuraCyan))
                                            isToday -> Brush.linearGradient(listOf(AuraIndigo, AuraSurfaceVariant))
                                            else -> Brush.linearGradient(listOf(AuraSurfaceVariant, AuraSurfaceVariant))
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isToday) AuraCyan else AuraCardBorder,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isToday) AuraCyan else TextTertiary,
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextTertiary,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuraMetricRow(
    label: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        )
    }
}
