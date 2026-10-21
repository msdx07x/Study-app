package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// STYLISH COOL AURA THEME PALETTE
val AuraBackground = Color(0xFF060814)
val AuraSurface = Color(0xFF0E132A)
val AuraSurfaceVariant = Color(0xFF161C3D)
val AuraCardBorder = Color(0xFF222B57)
val AuraGlowBorder = Color(0xFF384382)

// Luminous Aura Accents
val AuraCyan = Color(0xFF22D3EE)
val AuraCyanBright = Color(0xFF38BDF8)
val AuraPurple = Color(0xFFA855F7)
val AuraViolet = Color(0xFF8B5CF6)
val AuraIndigo = Color(0xFF6366F1)
val AuraPink = Color(0xFFEC4899)
val AuraEmerald = Color(0xFF10B981)
val AuraEmeraldBright = Color(0xFF34D399)
val AuraAmber = Color(0xFFFBBF24)

// High-contrast clean typography
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)

// Aura Gradient Brushes for stylish glowing surfaces & rings
val AuraPrimaryBrush = Brush.horizontalGradient(
    colors = listOf(AuraPurple, AuraIndigo, AuraCyan)
)

val AuraGlowCardBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF131938), Color(0xFF0A0E22))
)

val AuraAccentBrush = Brush.linearGradient(
    colors = listOf(AuraCyan, AuraEmeraldBright)
)

// Legacy alias mappings for backward compatibility
val DarkBackground = AuraBackground
val DarkSurface = AuraSurface
val DarkSurfaceVariant = AuraSurfaceVariant
val DarkCardBorder = AuraCardBorder
val ElectricBlue = AuraCyan
val DeepPrimary = AuraIndigo
val AccentTeal = AuraCyan
val AccentAmber = AuraAmber
val AccentEmerald = AuraEmeraldBright
val AccentRose = AuraPink
val ElectricBlueVariant = AuraCyanBright

// Light Theme Palette (if switched)
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightPrimary = Color(0xFF4F46E5)
val LightSecondary = Color(0xFF0891B2)
