package com.satyacheck.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

@Configuration
class MongoConfig {

    /**
     * Configures MongoDB validation listener to validate entities when they are saved
     */
    @Bean
    fun validatingMongoEventListener(factory: LocalValidatorFactoryBean) = ValidatingMongoEventListener(factory)

    /**
     * Remove _class field from MongoDB documents
     */
    @Bean
    fun mappingMongoConverter(
        mongoDatabaseFactory: MongoDatabaseFactory,
        mongoMappingContext: MongoMappingContext
    ): MappingMongoConverter {
        val dbRefResolver = DefaultDbRefResolver(mongoDatabaseFactory)
        val converter = MappingMongoConverter(dbRefResolver, mongoMappingContext)
        
        // Don't save _class to MongoDB
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        
        return converter
    }
}