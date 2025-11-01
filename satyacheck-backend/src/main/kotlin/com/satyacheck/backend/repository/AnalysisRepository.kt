package com.satyacheck.backend.repository

import com.satyacheck.backend.model.entity.Analysis
import com.satyacheck.backend.model.enum.Verdict
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AnalysisRepository : MongoRepository<Analysis, String> {
    fun findByUserId(userId: String, pageable: Pageable): Page<Analysis>
    
    fun findByUserIdAndTimestampBetween(
        userId: String, 
        start: LocalDateTime, 
        end: LocalDateTime, 
        pageable: Pageable
    ): Page<Analysis>
    
    fun findByVerdict(verdict: Verdict, pageable: Pageable): Page<Analysis>
    
    fun findByLanguage(language: String, pageable: Pageable): Page<Analysis>
    
    fun findByContentContainingIgnoreCase(searchTerm: String, pageable: Pageable): Page<Analysis>
}