package com.satyacheck.backend.model.enum

enum class Verdict {
    CREDIBLE,
    POTENTIALLY_MISLEADING,
    HIGH_MISINFORMATION_RISK,
    SCAM_ALERT,
    INSUFFICIENT_INFO,
    ERROR,
    UNKNOWN;
    
    companion object {
        fun fromString(value: String): Verdict {
            return when (value.lowercase()) {
                "credible" -> CREDIBLE
                "potentially misleading" -> POTENTIALLY_MISLEADING
                "high misinformation risk" -> HIGH_MISINFORMATION_RISK
                "scam alert" -> SCAM_ALERT
                "insufficient info" -> INSUFFICIENT_INFO
                "error" -> ERROR
                else -> UNKNOWN
            }
        }
    }
}