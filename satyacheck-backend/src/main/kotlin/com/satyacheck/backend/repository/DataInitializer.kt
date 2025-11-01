package com.satyacheck.backend.repository

import com.satyacheck.backend.model.entity.Article
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.util.logging.Logger

/**
 * Test component that initializes some sample data in MongoDB
 * This is a temporary component for development/testing purposes
 */
@Component
class DataInitializer(
    private val articleRepository: ArticleRepository
) : ApplicationRunner {
    private val logger = Logger.getLogger(DataInitializer::class.java.name)

    override fun run(args: ApplicationArguments?) {
        try {
            logger.info("Testing MongoDB connection and initializing sample data...")
            
            // Check if we have any articles already
            val count = articleRepository.count()
            logger.info("Current article count: $count")
            
            // Only add sample data if the repository is empty
            if (count == 0L) {
                logger.info("Adding sample articles to the database...")
                
                val sampleArticles = listOf(
                    Article(
                        slug = "digital-literacy",
                        title = "Understanding Digital Literacy",
                        description = "Learn about digital literacy and why it's important in today's information landscape.",
                        image = "article_digital_literacy",
                        category = "EDUCATION",
                        content = "Digital literacy refers to an individual's ability to find, evaluate, and compose clear information through writing and other media on various digital platforms. Digital literacy is evaluated by an individual's grammar, composition, typing skills and ability to produce writings, images, audio and designs using technology. Digital literacy does not replace traditional forms of literacy. Instead, it builds upon the foundation of traditional forms of literacy.",
                        language = "en"
                    ),
                    Article(
                        slug = "fact-checking",
                        title = "The Art of Fact-Checking",
                        description = "Essential skills for verifying information in the digital age.",
                        image = "article_verification",
                        category = "VERIFICATION",
                        content = "Fact-checking is the act of checking factual assertions in non-fictional text in order to determine the veracity and correctness of the factual statements in the text. This may be done either before (ante hoc) or after (post hoc) the text has been published or otherwise disseminated.",
                        language = "en"
                    ),
                    Article(
                        slug = "misinformation",
                        title = "Recognizing Misinformation",
                        description = "How to identify and avoid spreading false information online.",
                        image = "article_misinformation",
                        category = "VERIFICATION",
                        content = "Misinformation is false or inaccurate information that is spread unintentionally. It is distinguished from disinformation, which is deliberately deceptive. Rumors, gossip, and unverified reports are types of misinformation. The principal effect of misinformation is to elicit fear and suspicion among a population.",
                        language = "en"
                    )
                )
                
                articleRepository.saveAll(sampleArticles)
                logger.info("Successfully added ${sampleArticles.size} sample articles to the database")
            } else {
                logger.info("Database already contains articles, skipping sample data initialization")
            }
        } catch (e: Exception) {
            logger.severe("Failed to initialize sample data: ${e.message}")
            e.printStackTrace()
        }
    }
}