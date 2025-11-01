package com.satyacheck.android.presentation.screens.awareness

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.domain.model.QuizDifficulty
import com.satyacheck.android.domain.model.QuestionType
import com.satyacheck.android.domain.model.QuizQuestion
import com.satyacheck.android.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AwarenessScreen(
    onBackPressed: () -> Unit,
    viewModel: AwarenessViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Check Your Awareness") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (viewModel.quizState) {
                is QuizState.Welcome -> WelcomeContent(viewModel)
                is QuizState.Question -> QuestionContent(viewModel)
                is QuizState.LevelPassed -> LevelPassedContent(viewModel)
                is QuizState.LevelFailed -> LevelFailedContent(viewModel)
                is QuizState.AllLevelsPassed -> AllLevelsPassedContent(viewModel)
            }
            
            // Show loading indicator if loading
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeContent(viewModel: AwarenessViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome title
        Text(
            text = "Test Your Information Literacy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Description
        Text(
            text = "How well can you spot misinformation and verify facts? Take our quiz to find out!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Level cards
        QuizLevelCard(
            difficulty = QuizDifficulty.EASY,
            description = "Basic digital literacy and simple verification skills",
            onStartQuiz = { viewModel.startQuiz(QuizDifficulty.EASY) },
            isLocked = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizLevelCard(
            difficulty = QuizDifficulty.MEDIUM,
            description = "Intermediate fact-checking and source evaluation",
            onStartQuiz = { viewModel.startQuiz(QuizDifficulty.MEDIUM) },
            isLocked = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizLevelCard(
            difficulty = QuizDifficulty.HARD,
            description = "Advanced critical thinking and media analysis",
            onStartQuiz = { viewModel.startQuiz(QuizDifficulty.HARD) },
            isLocked = false
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        QuizLevelCard(
            difficulty = QuizDifficulty.EXTREME,
            description = "Expert-level information evaluation and deep analysis",
            onStartQuiz = { viewModel.startQuiz(QuizDifficulty.EXTREME) },
            isLocked = false
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Each level has 5 questions. You need to score at least 70% to advance to the next level.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuizLevelCard(
    difficulty: QuizDifficulty,
    description: String,
    onStartQuiz: () -> Unit,
    isLocked: Boolean
) {
    val backgroundColor = when (difficulty) {
        QuizDifficulty.EASY -> MaterialTheme.colorScheme.primaryContainer
        QuizDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
        QuizDifficulty.HARD -> MaterialTheme.colorScheme.tertiaryContainer
        QuizDifficulty.EXTREME -> MaterialTheme.colorScheme.errorContainer
    }
    
    val contentColor = when (difficulty) {
        QuizDifficulty.EASY -> MaterialTheme.colorScheme.onPrimaryContainer
        QuizDifficulty.MEDIUM -> MaterialTheme.colorScheme.onSecondaryContainer
        QuizDifficulty.HARD -> MaterialTheme.colorScheme.onTertiaryContainer
        QuizDifficulty.EXTREME -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onStartQuiz() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = difficulty.getDisplayName(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Difficulty indicator with stars
                Row {
                    repeat(when (difficulty) {
                        QuizDifficulty.EASY -> 1
                        QuizDifficulty.MEDIUM -> 2
                        QuizDifficulty.HARD -> 3
                        QuizDifficulty.EXTREME -> 4
                    }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartQuiz,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLocked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = backgroundColor
                )
            ) {
                Text(if (isLocked) "Locked" else "Start Quiz")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestionContent(viewModel: AwarenessViewModel) {
    val quiz = viewModel.currentQuiz ?: return
    val currentQuestion = quiz.questions.getOrNull(viewModel.currentQuestionIndex) ?: return
    
    var showExplanation by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(-1) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    
    // Reset state when question changes
    LaunchedEffect(currentQuestion) {
        showExplanation = false
        selectedOption = -1
        isAnswerSubmitted = false
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Quiz progress
        LinearProgressIndicator(
            progress = (viewModel.currentQuestionIndex + 1).toFloat() / quiz.questions.size,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Question counter and timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Question ${viewModel.currentQuestionIndex + 1}/${quiz.questions.size}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = viewModel.formatTime(viewModel.elapsedTime),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Difficulty indicator
        Text(
            text = "Difficulty: ${currentQuestion.difficulty.getDisplayName()}",
            style = MaterialTheme.typography.labelMedium,
            color = when (currentQuestion.difficulty) {
                QuizDifficulty.EASY -> MaterialTheme.colorScheme.primary
                QuizDifficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                QuizDifficulty.HARD -> MaterialTheme.colorScheme.tertiary
                QuizDifficulty.EXTREME -> MaterialTheme.colorScheme.error
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Question type badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = when (currentQuestion.type) {
                    QuestionType.MULTIPLE_CHOICE -> "Multiple Choice"
                    QuestionType.TRUE_FALSE -> "True or False"
                    QuestionType.SCENARIO -> "Scenario"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Question
        AnimatedContent(
            targetState = currentQuestion,
            transitionSpec = {
                slideInHorizontally(animationSpec = tween(300)) { fullWidth -> fullWidth } with
                slideOutHorizontally(animationSpec = tween(300)) { fullWidth -> -fullWidth }
            }
        ) { question ->
            Column {
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Options
                question.options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    val isCorrectAnswer = index == question.correctOptionIndex
                    val isIncorrectSelection = isSelected && !isCorrectAnswer
                    
                    val backgroundColor = when {
                        isAnswerSubmitted && isCorrectAnswer -> MaterialTheme.colorScheme.primaryContainer
                        isAnswerSubmitted && isIncorrectSelection -> MaterialTheme.colorScheme.errorContainer
                        isSelected -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    
                    val borderColor = when {
                        isAnswerSubmitted && isCorrectAnswer -> MaterialTheme.colorScheme.primary
                        isAnswerSubmitted && isIncorrectSelection -> MaterialTheme.colorScheme.error
                        isSelected -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    }
                    
                    val textColor = when {
                        isAnswerSubmitted && isCorrectAnswer -> MaterialTheme.colorScheme.onPrimaryContainer
                        isAnswerSubmitted && isIncorrectSelection -> MaterialTheme.colorScheme.onErrorContainer
                        isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !isAnswerSubmitted) {
                                selectedOption = index
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = backgroundColor,
                            contentColor = textColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Option letter (A, B, C, D)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) borderColor else MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A' + index).toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Show check or cross icon if answer is submitted
                            if (isAnswerSubmitted) {
                                Icon(
                                    imageVector = if (isCorrectAnswer) Icons.Default.Check else if (isIncorrectSelection) Icons.Default.Close else Icons.Default.Check,
                                    contentDescription = if (isCorrectAnswer) "Correct" else "Incorrect",
                                    tint = if (isCorrectAnswer) MaterialTheme.colorScheme.primary else if (isIncorrectSelection) MaterialTheme.colorScheme.error else Color.Transparent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Explanation (shown after selecting an answer)
        AnimatedVisibility(
            visible = showExplanation,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Explanation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explanation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = currentQuestion.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Submit/Next button
        Button(
            onClick = {
                if (selectedOption == -1) return@Button
                
                if (!isAnswerSubmitted) {
                    isAnswerSubmitted = true
                    showExplanation = true
                } else {
                    viewModel.answerQuestion(selectedOption)
                }
            },
            enabled = selectedOption != -1,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (!isAnswerSubmitted) "Submit Answer" else "Next Question"
            )
        }
    }
}

@Composable
fun LevelPassedContent(viewModel: AwarenessViewModel) {
    val quiz = viewModel.currentQuiz ?: return
    val analysis = viewModel.quizAnalysis ?: return
    val nextLevel = viewModel.currentDifficulty.getNextLevel()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Level Passed",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Congratulations text
        Text(
            text = "Congratulations!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You've passed the ${viewModel.currentDifficulty.getDisplayName()} level!",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Score details card
        ScoreDetailsCard(viewModel, analysis)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Next level button
        Button(
            onClick = { viewModel.proceedToNextLevel() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Proceed to ${nextLevel?.getDisplayName()} Level")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Return to menu button
        OutlinedButton(
            onClick = { viewModel.returnToWelcome() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Menu")
        }
    }
}

@Composable
fun LevelFailedContent(viewModel: AwarenessViewModel) {
    val quiz = viewModel.currentQuiz ?: return
    val analysis = viewModel.quizAnalysis ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Failure icon
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Level Failed",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(96.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Try again text
        Text(
            text = "Keep Learning!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You need ${(QuizDifficulty.passingThreshold * 100).toInt()}% to pass this level.",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Score details card
        ScoreDetailsCard(viewModel, analysis)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Retry button
        Button(
            onClick = { viewModel.retryCurrentLevel() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Return to menu button
        OutlinedButton(
            onClick = { viewModel.returnToWelcome() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Menu")
        }
    }
}

@Composable
fun AllLevelsPassedContent(viewModel: AwarenessViewModel) {
    val analysis = viewModel.quizAnalysis ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "All Levels Passed",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(96.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Congratulations text
        Text(
            text = "Master of Awareness!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You've conquered all difficulty levels. Impressive!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Score details card
        ScoreDetailsCard(viewModel, analysis)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Return to menu button
        Button(
            onClick = { viewModel.returnToWelcome() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Menu")
        }
    }
}

@Composable
fun ScoreDetailsCard(viewModel: AwarenessViewModel, analysis: com.satyacheck.android.domain.model.QuizAttempt) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Score percentage
            Text(
                text = "Your Score",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(analysis.score * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (analysis.passedThreshold) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(
                    title = "Correct",
                    value = "${analysis.answers.count { it.isCorrect }}/${analysis.answers.size}"
                )
                
                StatColumn(
                    title = "Time",
                    value = viewModel.formatTime(analysis.timeTaken)
                )
                
                StatColumn(
                    title = "Level",
                    value = viewModel.currentDifficulty.getDisplayName()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Strong areas
            Text(
                text = "Strong Areas",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            viewModel.getStrongAreas().forEach { area ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(area, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Weak areas
            Text(
                text = "Areas to Improve",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            viewModel.getWeakAreas().forEach { area ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = if (area.startsWith("Great job")) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(area, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun StatColumn(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}