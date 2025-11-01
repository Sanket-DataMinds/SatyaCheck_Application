package com.satyacheck.android.domain.model

enum class Verdict {
    CREDIBLE,
    POTENTIALLY_MISLEADING,
    HIGH_MISINFORMATION_RISK,
    SCAM_ALERT,
    UNKNOWN;
    
    fun toDisplayString(): String {
        return when (this) {
            CREDIBLE -> "Credible"
            POTENTIALLY_MISLEADING -> "Potentially Misleading"
            HIGH_MISINFORMATION_RISK -> "High Misinformation Risk"
            SCAM_ALERT -> "Scam Alert"
            UNKNOWN -> "Unknown"
        }
    }
    
    companion object {
        fun fromString(value: String): Verdict {
            val cleaned = value.uppercase().trim()
            return when {
                cleaned.contains("CREDIBLE") -> CREDIBLE
                cleaned.contains("POTENTIALLY_MISLEADING") || cleaned.contains("MISLEADING") -> POTENTIALLY_MISLEADING
                cleaned.contains("HIGH_MISINFORMATION_RISK") || cleaned.contains("MISINFORMATION") -> HIGH_MISINFORMATION_RISK
                cleaned.contains("SCAM_ALERT") || cleaned.contains("SCAM") -> SCAM_ALERT
                // Legacy support
                cleaned == "CREDIBLE" -> CREDIBLE
                cleaned == "POTENTIALLY MISLEADING" -> POTENTIALLY_MISLEADING
                cleaned == "HIGH MISINFORMATION RISK" -> HIGH_MISINFORMATION_RISK
                cleaned == "SCAM ALERT" -> SCAM_ALERT
                else -> CREDIBLE // Default to credible instead of unknown
            }
        }
    }
}
