package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: ExamViewModel = viewModel()
                    
                    when (val currentScreen = viewModel.currentScreen) {
                        is Screen.Dashboard, is Screen.ExamDetail -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToCreate = { viewModel.navigateTo(Screen.CreateExam) }
                            )
                        }
                        is Screen.CreateExam -> {
                            CreateExamScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }
                        is Screen.TakeExam -> {
                            TakeExamScreen(
                                viewModel = viewModel
                            )
                        }
                        is Screen.ExamResult -> {
                            ResultsScreen(
                                viewModel = viewModel,
                                onBackToDashboard = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        }
                    }
                }
            }
        }
    }
}
