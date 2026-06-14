package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuestionCard
import com.example.ui.components.TimerDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeExamScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier,
) {
    val exam by viewModel.selectedExam.collectAsState()
    val sections = viewModel.activeSectionsList
    val currentSectionIdx = viewModel.currentSectionIndex

    var showCancelDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showCancelDialog = true
    }

    if (exam == null || sections.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentSection = sections[currentSectionIdx]
    val qRange = remember(currentSection) {
        (currentSection.startQuestion..currentSection.endQuestion).toList()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.testTag("cancel_exam_arrow_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cancel and Exit",
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = exam!!.name,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = currentSection.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                        }
                    },
                    actions = {
                        exam!!.timerSeconds?.let {
                            TimerDisplay(seconds = viewModel.timeRemainingSeconds)
                        }
                        IconButton(
                            onClick = { viewModel.saveAndExit() },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("save_exit_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save & Exit",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Button(
                            onClick = { showSubmitDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("top_submit_button"),
                        ) {
                            Text(
                                text = "Submit",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val hasPrevious = currentSectionIdx > 0
                    OutlinedButton(
                        onClick = {
                            if (hasPrevious) {
                                viewModel.currentSectionIndex--
                            }
                        },
                        enabled = hasPrevious,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("prev_section_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Section",
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = "${currentSectionIdx + 1} of ${sections.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val hasNext = currentSectionIdx < sections.lastIndex
                    Button(
                        onClick = {
                            if (hasNext) {
                                viewModel.currentSectionIndex++
                            } else {
                                showSubmitDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (hasNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("next_section_button"),
                    ) {
                        Text(
                            text = if (hasNext) "Next" else "Finish",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (hasNext) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Section",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(qRange) { qNum ->
                QuestionCard(
                    questionNumber = qNum,
                    selectedOption = viewModel.activeAnswers[qNum] ?: 0,
                    onOptionSelect = { option ->
                        viewModel.activeAnswers[qNum] = option
                    },
                    onClear = {
                        viewModel.activeAnswers[qNum] = 0
                    },
                )
            }
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Quit Practice Exam?") },
                text = { Text("Your responses will be saved as a draft. You can continue later from where you left off.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showCancelDialog = false
                            viewModel.saveAndExit()
                        },
                        modifier = Modifier.testTag("save_and_exit_confirm_button"),
                    ) {
                        Text("Save & Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Continue Exam")
                    }
                },
            )
        }

        if (showSubmitDialog) {
            val hasKey = remember(exam) {
                exam?.let {
                    val mapStr = it.answersJson
                    mapStr.isNotBlank() && mapStr != "{}"
                } ?: false
            }
            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Submit Practice Responses") },
                text = {
                    val unanswered = viewModel.activeAnswers.values.count { it == 0 }
                    if (unanswered > 0) {
                        Text(
                            if (hasKey) "You have left $unanswered unanswered questions out of ${exam!!.totalQuestions}. Submit and grade now?"
                            else "You have left $unanswered unanswered questions out of ${exam!!.totalQuestions}. Save your responses?"
                        )
                    } else {
                        Text(
                            if (hasKey) "Are you ready to submit and grade your responses?"
                            else "Are you ready to submit and save your responses?"
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSubmitDialog = false
                            viewModel.submitExamAnswers()
                        },
                        modifier = Modifier.testTag("confirm_submit_button"),
                    ) {
                        Text(if (hasKey) "Submit & Grade" else "Submit & Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}
