package com.satyacheck.android.presentation.screens.analysis

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.presentation.theme.VerdictCredible
import com.satyacheck.android.presentation.theme.VerdictCredibleBackground
import com.satyacheck.android.presentation.theme.VerdictHighMisinformationRisk
import com.satyacheck.android.presentation.theme.VerdictHighMisinformationRiskBackground
import com.satyacheck.android.presentation.theme.VerdictPotentiallyMisleading
import com.satyacheck.android.presentation.theme.VerdictPotentiallyMisleadingBackground
import com.satyacheck.android.presentation.theme.VerdictScamAlert
import com.satyacheck.android.presentation.theme.VerdictScamAlertBackground

data class VerdictInfo(
    val icon: ImageVector,
    val textColor: Color,
    val backgroundColor: Color
)

fun getVerdictInfo(verdict: Verdict): VerdictInfo {
    return when (verdict) {
        Verdict.CREDIBLE -> VerdictInfo(
            icon = Icons.Filled.CheckCircle,
            textColor = VerdictCredible,
            backgroundColor = VerdictCredibleBackground
        )
        Verdict.POTENTIALLY_MISLEADING -> VerdictInfo(
            icon = Icons.Filled.Warning,
            textColor = VerdictPotentiallyMisleading,
            backgroundColor = VerdictPotentiallyMisleadingBackground
        )
        Verdict.HIGH_MISINFORMATION_RISK -> VerdictInfo(
            icon = Icons.Filled.Error,
            textColor = VerdictHighMisinformationRisk,
            backgroundColor = VerdictHighMisinformationRiskBackground
        )
        Verdict.SCAM_ALERT -> VerdictInfo(
            icon = Icons.Filled.Shield,
            textColor = VerdictScamAlert,
            backgroundColor = VerdictScamAlertBackground
        )
        Verdict.UNKNOWN -> VerdictInfo(
            icon = Icons.Filled.HelpCenter,
            textColor = Color.Gray,
            backgroundColor = Color.LightGray
        )
    }
}

fun getVerdictText(verdict: Verdict): String {
    return when (verdict) {
        Verdict.CREDIBLE -> "Credible"
        Verdict.POTENTIALLY_MISLEADING -> "Potentially Misleading"
        Verdict.HIGH_MISINFORMATION_RISK -> "High Misinformation Risk"
        Verdict.SCAM_ALERT -> "Scam Alert"
        Verdict.UNKNOWN -> "Unknown"
    }
}
