package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exam
import com.example.data.ExamSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeExamScreen(
    viewModel: ExamViewModel,
    modifier: Modifier = Modifier
) {
    val exam by viewModel.selectedExam.collectAsState()
    val sections = viewModel.activeSectionsList
    val currentSectionIdx = viewModel.currentSectionIndex

    var showCancelDialog by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    // Intercept hardware container back buttons
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
                            modifier = Modifier.testTag("cancel_exam_arrow_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Cancel and Exit"
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentSection.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1
                            )
                        }
                    },
                    actions = {
                        // Timer presentation
                        exam!!.timerSeconds?.let {
                            val secs = viewModel.timeRemainingSeconds
                            val hrs = secs / 3600
                            val mins = (secs % 3600) / 60
                            val finalSecs = secs % 60
                            
                            val formattedTime = String.format("%02d:%02d:%02d", hrs, mins, finalSecs)
                            
                            val isDanger = secs < 120 // less than 2 mins
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDanger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "⏱ $formattedTime",
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("exam_timer_display"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDanger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Save & Exit button
                        IconButton(
                            onClick = { viewModel.saveAndExit() },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("save_exit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save & Exit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Prominent top submit button
                        Button(
                            onClick = { showSubmitDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("top_submit_button")
                        ) {
                            Text(
                                text = "Submit",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        },
        bottomBar = {
            // Footers Navigation Pane
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
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
                            .testTag("prev_section_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Section",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Middle Progress pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "${currentSectionIdx + 1} of ${sections.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Next Button
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
                            contentColor = if (hasNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("next_section_button")
                    ) {
                        Text(
                            text = if (hasNext) "Next" else "Finish",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (hasNext) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Section",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(qRange) { qNum ->
                // Custom selectable option layout
                val selectedOpt = viewModel.activeAnswers[qNum] ?: 0
                QuestionOptionRow(
                    qNum = qNum,
                    selectedOption = selectedOpt,
                    onOptionSelect = { option ->
                        viewModel.activeAnswers[qNum] = option
                    },
                    onClear = {
                        viewModel.activeAnswers[qNum] = 0 // Clear back to blank State
                    }
                )
            }
        }

        // Cancel Exam Warning Dialog
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
                        modifier = Modifier.testTag("save_and_exit_confirm_button")
                    ) {
                        Text("Save & Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Continue Exam")
                    }
                }
            )
        }

        // Submit Answers Confirmation Dialog
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
                        modifier = Modifier.testTag("confirm_submit_button")
                    ) {
                        Text(if (hasKey) "Submit & Grade" else "Submit & Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSubmitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun QuestionOptionRow(
    qNum: Int,
    selectedOption: Int, // 1 to 4 or 0 (none)
    onOptionSelect: (Int) -> Unit,
    onClear: () -> Unit
) {
    val isAnswered = selectedOption != 0

    // Smoothly animate the card and question components fade-out states
    val cardBgColor by animateColorAsState(
        targetValue = if (isAnswered) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 350)
    )

    val cardBorderColor by animateColorAsState(
        targetValue = if (isAnswered) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = 350)
    )

    val animatedTextAlpha by animateFloatAsState(
        targetValue = if (isAnswered) 0.35f else 1f,
        animationSpec = tween(durationMillis = 350)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_row_$qNum"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question $qNum",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedTextAlpha)
                )
                
                // Clear button (only visible if an option is selected)
                if (isAnswered) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("clear_button_$qNum")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Answer",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Option selection row (Options 1, 2, 3, 4) styled with Material Design 3 circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (opt in 1..4) {
                    val isSelected = selectedOption == opt
                    
                    // Animate the size, background and text of each option dynamically
                    val animatedScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.18f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )

                    val bgOptColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = tween(durationMillis = 300)
                    )

                    val textOptColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isAnswered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(durationMillis = 300)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp) // Minimum touch target size & a perfect circle!
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                            }
                            .clip(CircleShape)
                            .background(bgOptColor)
                            .clickable { onOptionSelect(opt) }
                            .testTag("q_${qNum}_option_$opt"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$opt",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleMedium,
                            color = textOptColor
                        )
                    }
                }
            }
        }
    }
}
