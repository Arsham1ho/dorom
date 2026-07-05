package com.arsham.dorom.ui.theme

import androidx.compose.ui.graphics.Color

// Flat, no-gradient palette. One confident blue accent, everything else near-monochrome —
// deliberately Spotify-like: the accent carries all the color, surfaces are just brightness steps.
// Dark ("ink") surfaces
val InkBackground = Color(0xFF121212)
val InkSurface = Color(0xFF1A1A1A)
val InkSurfaceRaised = Color(0xFF282828)
val InkOutline = Color(0xFF3A3A3A)

// Light ("parchment") surfaces
val ParchmentBackground = Color(0xFFFFFFFF)
val ParchmentSurface = Color(0xFFF7F7F7)
val ParchmentSurfaceRaised = Color(0xFFEDEDED)
val ParchmentOutline = Color(0xFFDDDDDD)

val Terracotta = Color(0xFF2E7CF6)
val TerracottaDim = Color(0xFF1E4FA0)
val TerracottaOnLight = Color(0xFF1B5FD1)
val Sage = Color(0xFF6E7A8A)
val SageDim = Color(0xFF465061)
val SageOnLight = Color(0xFF4C5768)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)
val TextMuted = Color(0xFF7A7A7A)

val TextPrimaryLight = Color(0xFF121212)
val TextSecondaryLight = Color(0xFF5A5A5A)
val TextMutedLight = Color(0xFF8A8A8A)

val DangerRed = Color(0xFFE0453F)
val DangerRedOnLight = Color(0xFFC62F2A)
val WarnAmber = Color(0xFFD9A441)

// Neutral grayscale badge tints — Spotify keeps color reserved for the one accent, so per-tile
// "variety" comes from brightness steps instead of separate hues.
val BadgeBlue = Color(0xFF8A8A8A)
val BadgeViolet = Color(0xFF6E6E6E)
val BadgeGold = Color(0xFFA0A0A0)
