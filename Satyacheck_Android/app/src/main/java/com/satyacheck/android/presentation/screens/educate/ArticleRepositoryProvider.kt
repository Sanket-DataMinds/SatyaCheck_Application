package com.satyacheck.android.presentation.screens.educate

import androidx.lifecycle.ViewModel
import com.satyacheck.android.domain.repository.IArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel that only exists to provide an instance of IArticleRepository to composables
 * This is a workaround for injecting a repository into a composable with Hilt
 */
@HiltViewModel
class ArticleRepositoryProvider @Inject constructor(
    val articleRepository: IArticleRepository
) : ViewModel()