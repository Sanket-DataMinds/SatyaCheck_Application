package com.satyacheck.backend.config.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

/**
 * Custom health indicator for MongoDB
 */
@Component
class MongoHealthIndicator(private val mongoTemplate: MongoTemplate) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            // Try to execute a lightweight query to check if MongoDB is responsive
            mongoTemplate.count(Query(), "system.version")
            
            // If no exception is thrown, MongoDB is available
            Health.up()
                .withDetail("database", "MongoDB")
                .withDetail("status", "Available")
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("database", "MongoDB")
                .withDetail("status", "Unavailable")
                .withDetail("error", e.message)
                .build()
        }
    }
}