package com.satyacheck.backend.repository

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration

/**
 * Test configuration for MongoDB repositories using TestContainers
 */
@TestConfiguration
@EnableMongoRepositories(basePackages = ["com.satyacheck.backend.repository"])
class MongoTestConfiguration : AbstractMongoClientConfiguration() {

    companion object {
        // Reuse the same container across tests for efficiency
        private val MONGO_CONTAINER = MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .apply { start() }
    }

    override fun getDatabaseName(): String = "satyacheck-test"

    @Bean
    override fun mongoClient(): MongoClient {
        val connectionString = ConnectionString(MONGO_CONTAINER.replicaSetUrl)
        val mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .build()
        return MongoClients.create(mongoClientSettings)
    }

    @Bean
    fun mongoTemplate(mongoClient: MongoClient): MongoTemplate {
        return MongoTemplate(mongoClient, databaseName)
    }
}