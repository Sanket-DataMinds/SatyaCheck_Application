package com.satyacheck.android.data.repository

import com.satyacheck.android.domain.model.Quiz
import com.satyacheck.android.domain.model.QuizDifficulty
import com.satyacheck.android.domain.model.QuizQuestion
import com.satyacheck.android.domain.model.QuestionType

/**
 * Repository for quiz data
 * This is a mock implementation with hardcoded quizzes that will be replaced
 * when the backend is integrated
 */
class QuizRepository {
    
    /**
     * Get quizzes for a specific difficulty level
     */
    fun getQuizzesByDifficulty(difficulty: QuizDifficulty, languageCode: String = "en"): Quiz {
        // In a real implementation, this would fetch from an API or local database
        // For now, we're returning mock data
        return when (difficulty) {
            QuizDifficulty.EASY -> getEasyQuiz(languageCode)
            QuizDifficulty.MEDIUM -> getMediumQuiz(languageCode)
            QuizDifficulty.HARD -> getHardQuiz(languageCode)
            QuizDifficulty.EXTREME -> getExtremeQuiz(languageCode)
        }
    }
    
    /**
     * Mock data for easy difficulty quiz
     */
    private fun getEasyQuiz(languageCode: String): Quiz {
        return Quiz(
            id = "easy-quiz-1",
            title = "Basic Digital Literacy",
            difficulty = QuizDifficulty.EASY,
            questions = listOf(
                QuizQuestion(
                    id = "q1-easy",
                    questionText = "What does the padlock icon in your web browser's address bar indicate?",
                    options = listOf(
                        "The website is very popular",
                        "The website uses secure encryption (HTTPS)",
                        "The website is owned by a large company",
                        "The website has no advertisements"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.EASY,
                    explanation = "The padlock icon indicates that the website uses HTTPS encryption, which helps protect your data."
                ),
                QuizQuestion(
                    id = "q2-easy",
                    questionText = "True or False: You should always share your password with tech support when they call you.",
                    options = listOf(
                        "True",
                        "False"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.TRUE_FALSE,
                    difficulty = QuizDifficulty.EASY,
                    explanation = "Legitimate tech support will never ask for your password. This is a common phishing tactic."
                ),
                QuizQuestion(
                    id = "q3-easy",
                    questionText = "What is 'phishing'?",
                    options = listOf(
                        "A hobby involving catching fish",
                        "A technique hackers use to steal passwords and personal information",
                        "A type of computer virus",
                        "A method to speed up your internet"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.EASY,
                    explanation = "Phishing is when attackers pose as trustworthy entities to trick you into revealing sensitive information."
                ),
                QuizQuestion(
                    id = "q4-easy",
                    questionText = "You receive an email saying you've won a lottery you never entered. What should you do?",
                    options = listOf(
                        "Click the link to claim your prize",
                        "Reply with your bank details",
                        "Delete the email - it's likely a scam",
                        "Forward it to all your friends"
                    ),
                    correctOptionIndex = 2,
                    type = QuestionType.SCENARIO,
                    difficulty = QuizDifficulty.EASY,
                    explanation = "Unexpected lottery wins, especially ones you never entered, are almost always scams."
                ),
                QuizQuestion(
                    id = "q5-easy",
                    questionText = "True or False: Social media posts are reliable sources of news and information.",
                    options = listOf(
                        "True",
                        "False"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.TRUE_FALSE,
                    difficulty = QuizDifficulty.EASY,
                    explanation = "Social media posts may contain misinformation. It's important to verify information from multiple reliable sources."
                )
            )
        )
    }
    
    /**
     * Mock data for medium difficulty quiz
     */
    private fun getMediumQuiz(languageCode: String): Quiz {
        return Quiz(
            id = "medium-quiz-1",
            title = "Intermediate Information Verification",
            difficulty = QuizDifficulty.MEDIUM,
            questions = listOf(
                QuizQuestion(
                    id = "q1-medium",
                    questionText = "Which of these is a sign of a potentially fake news website?",
                    options = listOf(
                        "The site has an 'About Us' page",
                        "The site contains contact information",
                        "The site URL mimics a known news site but with slight changes",
                        "The site displays the date of publication"
                    ),
                    correctOptionIndex = 2,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.MEDIUM,
                    explanation = "Fake news sites often mimic legitimate news sources with slight URL changes, like 'ABCNews.com.co' instead of the real 'ABCNews.com'."
                ),
                QuizQuestion(
                    id = "q2-medium",
                    questionText = "What should you check when evaluating the credibility of an article?",
                    options = listOf(
                        "How many images it contains",
                        "The author's credentials and the publication date",
                        "How long the article is",
                        "The website's color scheme"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.MEDIUM,
                    explanation = "Author credentials and publication date help establish credibility and relevance of the information."
                ),
                QuizQuestion(
                    id = "q3-medium",
                    questionText = "Your friend shares a shocking news story on social media. The best first step to verify it is:",
                    options = listOf(
                        "Share it immediately so others know",
                        "Search for the same story from multiple reliable sources",
                        "Ask your friend where they found it",
                        "Ignore it as it's probably false"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.SCENARIO,
                    difficulty = QuizDifficulty.MEDIUM,
                    explanation = "Cross-checking information with multiple reliable sources is a key verification technique."
                ),
                QuizQuestion(
                    id = "q4-medium",
                    questionText = "True or False: If an email address looks legitimate (e.g., support@amazon.com), then the email is definitely from that company.",
                    options = listOf(
                        "True",
                        "False"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.TRUE_FALSE,
                    difficulty = QuizDifficulty.MEDIUM,
                    explanation = "Email addresses can be spoofed to appear legitimate. Always check email headers and be cautious of unexpected emails asking for information."
                ),
                QuizQuestion(
                    id = "q5-medium",
                    questionText = "What is 'confirmation bias' in the context of misinformation?",
                    options = listOf(
                        "When websites confirm your login details",
                        "When you only believe information that confirms what you already think",
                        "When multiple sources verify the same facts",
                        "When an expert confirms a news story"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.MEDIUM,
                    explanation = "Confirmation bias is our tendency to favor information that confirms our existing beliefs, making us vulnerable to misinformation."
                )
            )
        )
    }
    
    /**
     * Mock data for hard difficulty quiz
     */
    private fun getHardQuiz(languageCode: String): Quiz {
        return Quiz(
            id = "hard-quiz-1",
            title = "Advanced Critical Thinking",
            difficulty = QuizDifficulty.HARD,
            questions = listOf(
                QuizQuestion(
                    id = "q1-hard",
                    questionText = "Which technique is NOT typically used in deepfake creation?",
                    options = listOf(
                        "Generative Adversarial Networks (GANs)",
                        "Face swapping algorithms",
                        "Manual frame-by-frame editing",
                        "Quantum encryption protocols"
                    ),
                    correctOptionIndex = 3,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.HARD,
                    explanation = "Quantum encryption protocols are used for secure communication, not for creating deepfakes. Deepfakes typically use AI techniques like GANs."
                ),
                QuizQuestion(
                    id = "q2-hard",
                    questionText = "Which of these is a reliable method to verify if an image has been manipulated?",
                    options = listOf(
                        "Check if the image looks realistic",
                        "Conduct a reverse image search to find the original source",
                        "Ask friends if they think it's real",
                        "Trust the caption provided with the image"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.HARD,
                    explanation = "Reverse image searches can help find the original source of an image and reveal if it has been altered or taken out of context."
                ),
                QuizQuestion(
                    id = "q3-hard",
                    questionText = "You receive a message that your bank account has been compromised with a link to verify your identity. The message has the bank's logo and correct formatting. What's the best action?",
                    options = listOf(
                        "Click the link and enter your details to secure your account",
                        "Reply with your account number so they can verify you",
                        "Contact your bank directly using the phone number from their official website",
                        "Forward the message to your friends to warn them"
                    ),
                    correctOptionIndex = 2,
                    type = QuestionType.SCENARIO,
                    difficulty = QuizDifficulty.HARD,
                    explanation = "Never click links or respond to suspicious messages. Always contact your bank through official channels you've independently verified."
                ),
                QuizQuestion(
                    id = "q4-hard",
                    questionText = "True or False: If statistical data is presented in a chart or graph, it's generally accurate and unbiased.",
                    options = listOf(
                        "True",
                        "False"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.TRUE_FALSE,
                    difficulty = QuizDifficulty.HARD,
                    explanation = "Charts and graphs can be manipulated to present data in misleading ways by changing scales, omitting data points, or selecting specific timeframes."
                ),
                QuizQuestion(
                    id = "q5-hard",
                    questionText = "What is 'source triangulation' in fact-checking?",
                    options = listOf(
                        "Checking three different social media platforms",
                        "Verifying information across multiple independent, reliable sources",
                        "Creating a triangle diagram to organize information",
                        "A technique used only by professional journalists"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.HARD,
                    explanation = "Source triangulation involves cross-checking information across multiple independent, reliable sources to confirm its accuracy."
                )
            )
        )
    }
    
    /**
     * Mock data for extreme difficulty quiz
     */
    private fun getExtremeQuiz(languageCode: String): Quiz {
        return Quiz(
            id = "extreme-quiz-1",
            title = "Expert Information Analysis",
            difficulty = QuizDifficulty.EXTREME,
            questions = listOf(
                QuizQuestion(
                    id = "q1-extreme",
                    questionText = "Which of the following best describes 'astroturfing' in the context of misinformation?",
                    options = listOf(
                        "Using artificial grass in advertising materials",
                        "Masking sponsored content as organic user-generated content",
                        "A technique to grow social media followers quickly",
                        "Creating fake websites with green themes"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.EXTREME,
                    explanation = "Astroturfing is when organizations or individuals create the false impression of widespread grassroots support for a policy, individual, or product."
                ),
                QuizQuestion(
                    id = "q2-extreme",
                    questionText = "A news article cites 'experts say' without naming specific experts or studies. This is an example of:",
                    options = listOf(
                        "Proper journalistic anonymity",
                        "A weasel word technique that undermines credibility",
                        "Standard citation practice",
                        "An effective way to protect expert identities"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.EXTREME,
                    explanation = "Using vague attributions like 'experts say' without specific sources is a weasel word technique that makes claims appear authoritative without actual substantiation."
                ),
                QuizQuestion(
                    id = "q3-extreme",
                    questionText = "True or False: An article containing one factual error means all information in it should be disregarded.",
                    options = listOf(
                        "True",
                        "False"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.TRUE_FALSE,
                    difficulty = QuizDifficulty.EXTREME,
                    explanation = "While errors raise concerns about overall accuracy, individual facts within an article can still be correct. Each claim should be evaluated independently."
                ),
                QuizQuestion(
                    id = "q4-extreme",
                    questionText = "You're analyzing coverage of a political issue across multiple news sources. You notice different outlets emphasize different facts while using the same underlying data. This is primarily an example of:",
                    options = listOf(
                        "Fake news",
                        "Framing bias",
                        "Statistical error",
                        "Illegal misrepresentation"
                    ),
                    correctOptionIndex = 1,
                    type = QuestionType.SCENARIO,
                    difficulty = QuizDifficulty.EXTREME,
                    explanation = "Framing bias occurs when media outlets selectively emphasize certain facts while downplaying others to influence how audiences interpret an issue, even when using accurate data."
                ),
                QuizQuestion(
                    id = "q5-extreme",
                    questionText = "Which technique helps identify potential AI-generated text?",
                    options = listOf(
                        "Checking for perfect grammar",
                        "Looking for consistent logical flow throughout",
                        "Searching for repetitive phrases and unusual phrasing patterns",
                        "Counting the number of paragraphs"
                    ),
                    correctOptionIndex = 2,
                    type = QuestionType.MULTIPLE_CHOICE,
                    difficulty = QuizDifficulty.EXTREME,
                    explanation = "AI-generated text often contains repetitive phrases, unusual phrasing, and can be overly formal or exhibit inconsistent knowledge depth across different sections of text."
                )
            )
        )
    }
}