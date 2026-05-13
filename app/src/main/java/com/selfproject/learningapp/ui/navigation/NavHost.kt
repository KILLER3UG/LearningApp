package com.selfproject.learningapp.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.selfproject.learningapp.ui.screens.DocumentScreen
import com.selfproject.learningapp.ui.screens.HomeScreen
import com.selfproject.learningapp.ui.settings.SettingsScreen
import com.selfproject.learningapp.ui.theme.StudyNotesAnimations
import com.selfproject.learningapp.viewmodel.HomeViewModel
import com.selfproject.learningapp.viewmodel.MainViewModel

/**
 * Navigation routes for the app.
 *
 * Issue 1: Home screen added — note list with subjects and swipe actions.
 * App flow: HomeScreen → DocumentScreen → SettingsScreen
 */
sealed class Screen {
    data object Home : Screen()
    data object Document : Screen()
    data object Settings : Screen()

    private companion object {
        val order = listOf(Home, Document, Settings)
    }
    val index: Int get() = order.indexOf(this)
}

@Composable
fun LearningAppNavHost() {
    val mainViewModel: MainViewModel = viewModel()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    // Selected note to open — passed from HomeScreen to DocumentScreen
    var selectedNoteId by remember { mutableStateOf<String?>(null) }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            val forward = targetState.index > initialState.index
            slideInHorizontally(
                initialOffsetX = { fullWidth -> if (forward) fullWidth else -fullWidth },
                animationSpec = tween(StudyNotesAnimations.DurationSlow, easing = StudyNotesAnimations.EasingStandard)
            ) + fadeIn(
                animationSpec = tween(StudyNotesAnimations.DurationNormal)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> if (forward) -fullWidth else fullWidth },
                animationSpec = tween(StudyNotesAnimations.DurationSlow, easing = StudyNotesAnimations.EasingStandard)
            ) + fadeOut(
                animationSpec = tween(StudyNotesAnimations.DurationNormal)
            )
        },
        label = "nav_transition"
    ) { screen ->
        when (screen) {
            // Issue 1: Home screen — note list with subject filter pills, swipe-to-pin/delete
            Screen.Home -> {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNoteClick = { note ->
                        selectedNoteId = note.id
                        currentScreen = Screen.Document
                        mainViewModel.loadDocument(android.net.Uri.parse(note.id))
                    },
                    onSettingsClick = { currentScreen = Screen.Settings }
                )
            }
            Screen.Document -> DocumentScreen(
                viewModel = mainViewModel,
                onNavigateToHome = { currentScreen = Screen.Home },
                onNavigateToSettings = { currentScreen = Screen.Settings }
            )
            Screen.Settings -> SettingsScreen(
                onNavigateBack = {
                    // After settings, go back to either Home or Document depending on state
                    currentScreen = if (selectedNoteId != null) Screen.Document else Screen.Home
                }
            )
        }
    }
}