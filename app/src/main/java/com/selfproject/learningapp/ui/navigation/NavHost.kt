package com.selfproject.learningapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.selfproject.learningapp.ui.screens.DocumentScreen
import com.selfproject.learningapp.viewmodel.MainViewModel

@Composable
fun LearningAppNavHost() {
    val viewModel: MainViewModel = viewModel()
    DocumentScreen(viewModel = viewModel)
}
