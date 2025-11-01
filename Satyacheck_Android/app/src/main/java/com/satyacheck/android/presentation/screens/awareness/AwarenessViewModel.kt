package com.satyacheck.android.presentation.screens.awareness

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.QuizRepository
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.domain.model.Quiz
import com.satyacheck.android.domain.model.QuizAnswer
import com.satyacheck.android.domain.model.QuizAttempt
import com.satyacheck.android.domain.model.QuizDifficulty
import com.satyacheck.android.domain.model.QuizQuestion
import com.satyacheck.android.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AwarenessViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    // Repository for quiz data
    private val quizRepository = QuizRepository()
    
    // Current state of the quiz flow
    var quizState by mutableStateOf<QuizState>(QuizState.Welcome)
        private set
    
    // Current quiz
    var currentQuiz by mutableStateOf<Quiz?>(null)
        private set
    
    // Current question index
    var currentQuestionIndex by mutableStateOf(0)
        private set
    
    // User's answers
    var userAnswers by mutableStateOf<MutableList<QuizAnswer>>(mutableListOf())
        private set
    
    // Timer for quiz
    var elapsedTime by mutableStateOf(0L)
        private set
    
    // Current difficulty level
    var currentDifficulty by mutableStateOf(QuizDifficulty.EASY)
        private set
    
    // Loading state
    var isLoading by mutableStateOf(false)
        private set
    
    // Quiz result analysis
    var quizAnalysis by mutableStateOf<QuizAttempt?>(null)
        private set
    
    // Timer job
    private var timerJob: kotlinx.coroutines.Job? = null
    
    // Start time for the current question
    private var questionStartTime = 0L
    
    /**
     * Start the quiz with the given difficulty level
     */
    fun startQuiz(difficulty: QuizDifficulty = QuizDifficulty.EASY, languageCode: String = "en") {
        isLoading = true
        currentDifficulty = difficulty
        
        // In a real implementation, this would be an API call
        // For now, we're using mock data from the repository
        currentQuiz = quizRepository.getQuizzesByDifficulty(difficulty, languageCode)
        
        // Reset state
        currentQuestionIndex = 0
        userAnswers = mutableListOf()
        elapsedTime = 0L
        quizState = QuizState.Question
        questionStartTime = System.currentTimeMillis()
        
        // Start the timer
        startTimer()
        
        isLoading = false
    }
    
    /**
     * Go to the next question or finish the quiz
     */
    fun goToNextQuestion() {
        if (currentQuiz == null) return
        
        if (currentQuestionIndex < (currentQuiz?.questions?.size ?: 0) - 1) {
            currentQuestionIndex++
            questionStartTime = System.currentTimeMillis()
        } else {
            finishQuiz()
        }
    }
    
    /**
     * Answer the current question
     */
    fun answerQuestion(selectedOptionIndex: Int) {
        currentQuiz?.questions?.getOrNull(currentQuestionIndex)?.let { question ->
            val isCorrect = selectedOptionIndex == question.correctOptionIndex
            val timeSpent = System.currentTimeMillis() - questionStartTime
            
            val answer = QuizAnswer(
                questionId = question.id,
                selectedOptionIndex = selectedOptionIndex,
                isCorrect = isCorrect,
                timeSpent = timeSpent
            )
            
            userAnswers.add(answer)
            goToNextQuestion()
        }
    }
    
    /**
     * Finish the current quiz and calculate results
     */
    private fun finishQuiz() {
        timerJob?.cancel()
        
        currentQuiz?.let { quiz ->
            val correctAnswers = userAnswers.count { it.isCorrect }
            val totalQuestions = quiz.questions.size
            val score = correctAnswers.toFloat() / totalQuestions
            
            val attempt = QuizAttempt(
                quizId = quiz.id,
                answers = userAnswers.toList(),
                score = score,
                timeTaken = elapsedTime,
                passedThreshold = score >= QuizDifficulty.passingThreshold
            )
            
            // Save quiz completion to user preferences
            viewModelScope.launch {
                userPreferencesRepository.addCompletedQuiz(quiz.id)
            }
            
            quizAnalysis = attempt
            quizState = if (score >= QuizDifficulty.passingThreshold && currentDifficulty.getNextLevel() != null) {
                QuizState.LevelPassed
            } else if (score >= QuizDifficulty.passingThreshold) {
                QuizState.AllLevelsPassed
            } else {
                QuizState.LevelFailed
            }
        }
    }
    
    /**
     * Start a timer to track elapsed time
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                elapsedTime += 1000
            }
        }
    }
    
    /**
     * Proceed to the next level
     */
    fun proceedToNextLevel() {
        currentDifficulty.getNextLevel()?.let { nextDifficulty ->
            startQuiz(nextDifficulty)
        }
    }
    
    /**
     * Retry the current level
     */
    fun retryCurrentLevel() {
        startQuiz(currentDifficulty)
    }
    
    /**
     * Return to the welcome screen
     */
    fun returnToWelcome() {
        timerJob?.cancel()
        quizState = QuizState.Welcome
    }
    
    /**
     * Calculate strong and weak areas based on user answers
     */
    fun getStrongAreas(): List<String> {
        val strongAreas = mutableListOf<String>()
        
        // Group correct answers by question type
        val correctAnswersByType = userAnswers
            .filter { it.isCorrect }
            .mapNotNull { answer ->
                currentQuiz?.questions?.find { it.id == answer.questionId }?.type?.name
            }
            .groupingBy { it }
            .eachCount()
        
        // Identify strong areas based on correct answers
        if (correctAnswersByType["MULTIPLE_CHOICE"] ?: 0 >= 2) {
            strongAreas.add("Knowledge-based questions")
        }
        
        if (correctAnswersByType["TRUE_FALSE"] ?: 0 >= 1) {
            strongAreas.add("Fact verification")
        }
        
        if (correctAnswersByType["SCENARIO"] ?: 0 >= 1) {
            strongAreas.add("Practical application")
        }
        
        return strongAreas.ifEmpty { listOf("Keep practicing to build your strengths!") }
    }
    
    /**
     * Get weak areas based on user answers
     */
    fun getWeakAreas(): List<String> {
        val weakAreas = mutableListOf<String>()
        
        // Group incorrect answers by question type
        val incorrectAnswersByType = userAnswers
            .filter { !it.isCorrect }
            .mapNotNull { answer ->
                currentQuiz?.questions?.find { it.id == answer.questionId }?.type?.name
            }
            .groupingBy { it }
            .eachCount()
        
        // Identify weak areas based on incorrect answers
        if (incorrectAnswersByType["MULTIPLE_CHOICE"] ?: 0 >= 2) {
            weakAreas.add("Knowledge-based questions")
        }
        
        if (incorrectAnswersByType["TRUE_FALSE"] ?: 0 >= 1) {
            weakAreas.add("Fact verification")
        }
        
        if (incorrectAnswersByType["SCENARIO"] ?: 0 >= 1) {
            weakAreas.add("Practical application")
        }
        
        return weakAreas.ifEmpty { listOf("Great job! No significant weak areas identified.") }
    }
    
    /**
     * Format time as mm:ss
     */
    fun formatTime(timeInMs: Long): String {
        val totalSeconds = timeInMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * States for the quiz flow
 */
sealed class QuizState {
    object Welcome : QuizState()
    object Question : QuizState()
    object LevelPassed : QuizState()
    object LevelFailed : QuizState()
    object AllLevelsPassed : QuizState()
}