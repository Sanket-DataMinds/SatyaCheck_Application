package com.satyacheck.android.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary colors - Google Blue inspired palette
val Primary = Color(0xFF1A73E8)       // Google Blue
val PrimaryDark = Color(0xFF0D47A1)   // Darker shade
val PrimaryLight = Color(0xFFD0E0FC)  // Lighter tint

// Secondary colors - Google orange-inspired
val Secondary = Color(0xFFFA7B17)     // Google Orange 
val SecondaryDark = Color(0xFFE65100) // Darker orange
val SecondaryLight = Color(0xFFFFCCBC) // Lighter orange tint

// Background and surface colors - Light Theme (Google Material 3)
val BackgroundLight = Color(0xFFF8F9FA) // Google background light
val SurfaceLight = Color(0xFFFFFFFF)    // White surface
val SurfaceVariantLight = Color(0xFFE8EAED) // Google light surface variant

// Background and surface colors - Dark Theme (Google Material 3)
val BackgroundDark = Color(0xFF202124)  // Google dark background
val SurfaceDark = Color(0xFF303134)     // Google dark surface
val SurfaceVariantDark = Color(0xFF3C4043) // Google dark surface variant

// Text colors - Light Theme (Google Material 3)
val TextPrimaryLight = Color(0xFF202124)  // Google primary text dark
val TextSecondaryLight = Color(0xFF5F6368) // Google secondary text
val TextTertiaryLight = Color(0xFF9AA0A6)  // Google tertiary text

// Text colors - Dark Theme (Google Material 3)
val TextPrimaryDark = Color(0xFFE8EAED)   // Google primary text light
val TextSecondaryDark = Color(0xFFBDC1C6) // Google secondary text light
val TextTertiaryDark = Color(0xFF9AA0A6)  // Google tertiary text light

// Error colors (Google Material 3)
val Error = Color(0xFFD93025)         // Google Red
val ErrorLight = Color(0xFFFCE8E6)    // Light red background
val ErrorDark = Color(0xFFB3261E)     // Dark red for dark theme

// Success colors
val Success = Color(0xFF1E8E3E)       // Google Green
val SuccessLight = Color(0xFFCEEAD6)  // Light green background
val SuccessDark = Color(0xFF137333)   // Dark green

// Warning colors
val Warning = Color(0xFFFABB05)       // Google Yellow
val WarningLight = Color(0xFFFEF7E0)  // Light yellow background
val WarningDark = Color(0xFFEA8600)   // Dark yellow

// Info colors
val Info = Color(0xFF1A73E8)          // Google Blue (same as primary)
val InfoLight = Color(0xFFE8F0FE)     // Light blue background
val InfoDark = Color(0xFF185ABC)      // Dark blue

// Verdict colors using Google palette
val VerdictCredible = Success
val VerdictPotentiallyMisleading = Warning
val VerdictHighMisinformationRisk = Error
val VerdictScamAlert = Info

// Verdict background colors - Light Theme
val VerdictCredibleBackground = SuccessLight
val VerdictPotentiallyMisleadingBackground = WarningLight
val VerdictHighMisinformationRiskBackground = ErrorLight
val VerdictScamAlertBackground = InfoLight

// Verdict background colors - Dark Theme
val VerdictCredibleBackgroundDark = Color(0xFF0D2E12)
val VerdictPotentiallyMisleadingBackgroundDark = Color(0xFF332D00)
val VerdictHighMisinformationRiskBackgroundDark = Color(0xFF3B0000)
val VerdictScamAlertBackgroundDark = Color(0xFF082447)

// Legacy references for backward compatibility
val Background = BackgroundLight
val Surface = SurfaceLight
val SurfaceVariant = SurfaceVariantLight
val TextPrimary = TextPrimaryLight
val TextSecondary = TextSecondaryLight
val TextTertiary = TextTertiaryLight

// Backward compatibility for Accent
val Accent = Secondary
val AccentDark = SecondaryDark
val AccentLight = SecondaryLight
