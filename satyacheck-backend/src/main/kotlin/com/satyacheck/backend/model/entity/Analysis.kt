package com.satyacheck.backend.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed
import com.satyacheck.backend.model.enum.Verdict
import java.time.LocalDateTime

@Document(collection = "analyses")
data class Analysis(
    @Id
    val id: String? = null,
    
    @Indexed
    val content: String,
    
    val verdict: Verdict,
    
    val explanation: String,
    
    @Indexed
    val timestamp: LocalDateTime = LocalDateTime.now(),
    
    val metadata: Map<String, Any> = emptyMap(),
    
    val sourceUrl: String? = null,
    
    val language: String = "en",
    
    @Indexed
    val userId: String? = null
)