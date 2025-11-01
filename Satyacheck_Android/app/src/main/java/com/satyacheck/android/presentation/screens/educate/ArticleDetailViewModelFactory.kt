package com.satyacheck.android.presentation.screens.educate

import javax.inject.Inject
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Factory for creating ArticleDetailViewModel with assisted injection
 * This class is injected into the composable as a Hilt ViewModel
 */
@ViewModelScoped
class ArticleDetailViewModelFactory @Inject constructor(
    val factory: ArticleDetailViewModel.Factory
)