package com.satyacheck.android.domain.model

/**
 * Defines the difficulty levels for quizzes
 */
enum class QuizDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXTREME;
    
    fun getNextLevel(): QuizDifficulty? {
        return when (this) {
            EASY -> MEDIUM
            MEDIUM -> HARD
            HARD -> EXTREME
            EXTREME -> null
        }
    }
    
    fun getDisplayName(): String {
        return when (this) {
            EASY -> "Easy"
            MEDIUM -> "Medium"
            HARD -> "Hard"
            EXTREME -> "Extreme"
        }
    }
    
    companion object {
        val passingThreshold = 0.7f // 70% passing threshold
    }
}

/**
 * Types of quiz questions
 */
enum class QuestionType {
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    SCENARIO
}

/**
 * Data model for quiz questions
 */
data class QuizQuestion(
    val id: String,                       // Unique identifier for the question
    val questionText: String,             // The question being asked
    val options: List<String>,            // Possible answers
    val correctOptionIndex: Int,          // Index of the correct answer in the options list
    val type: QuestionType,               // Type of question (multiple choice, true/false, scenario)
    val difficulty: QuizDifficulty,       // Difficulty level of this question
    val explanation: String               // Explanation of the correct answer
)

/**
 * Data model for a complete quiz
 */
data class Quiz(
    val id: String,                       // Unique identifier for the quiz
    val title: String,                    // Title of the quiz
    val difficulty: QuizDifficulty,       // Difficulty level
    val questions: List<QuizQuestion>,    // List of questions in this quiz
    val timeLimit: Int? = null            // Optional time limit in seconds
)

/**
 * Data model for user's answer to a specific question
 */
data class QuizAnswer(
    val questionId: String,               // ID of the question being answered
    val selectedOptionIndex: Int,         // Index of the user's selected option
    val isCorrect: Boolean,               // Whether the answer is correct
    val timeSpent: Long? = null           // Optional time spent on this question in milliseconds
)

/**
 * Data model for a completed quiz attempt
 */
data class QuizAttempt(
    val quizId: String,                   // ID of the quiz that was attempted
    val answers: List<QuizAnswer>,        // User's answers for each question
    val score: Float,                     // Score as a percentage (0.0 - 1.0)
    val timeTaken: Long,                  // Total time taken in milliseconds
    val passedThreshold: Boolean,         // Whether the score meets the passing threshold
    val timestamp: Long = System.currentTimeMillis() // When this attempt was completed
)

/**
 * Data model for quiz results analysis
 */
data class QuizAnalysis(
    val correctAnswers: Int,
    val totalQuestions: Int,
    val score: Float,
    val timeTaken: Long,
    val difficulty: QuizDifficulty,
    val strongAreas: List<String>,
    val weakAreas: List<String>
)