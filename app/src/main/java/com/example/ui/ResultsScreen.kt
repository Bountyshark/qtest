package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exam
import com.example.data.ExamAttempt
import com.example.data.ExamSection
import com.example.data.JsonSerializer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: ExamViewModel,
    onBackToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val examState by viewModel.selectedExam.collectAsState()
    val attemptState by viewModel.selectedAttempt.collectAsState()

    val currentExam = examState
    val currentAttempt = attemptState

    if (currentExam == null || currentAttempt == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    androidx.activity.compose.BackHandler {
        onBackToDashboard()
    }

    val attemptAnswers = remember(currentAttempt) { JsonSerializer.deserializeMap(currentAttempt.answersJson) }
    val correctKeyAnswers = remember(currentExam) { JsonSerializer.deserializeMap(currentExam.answersJson) }
    val sections = remember(currentExam) { JsonSerializer.deserializeSections(currentExam.sectionsJson) }

    val hasKey = remember(correctKeyAnswers) { correctKeyAnswers.isNotEmpty() }

    // Let's compute section individual analytical numbers
    val sectionsAnalytics = remember(sections, attemptAnswers, correctKeyAnswers) {
        sections.map { sec ->
            val range = sec.startQuestion..sec.endQuestion
            var correct = 0
            var incorrect = 0
            var empty = 0
            
            for (q in range) {
                val ans = attemptAnswers[q] ?: 0
                val correctAns = correctKeyAnswers[q] ?: 0
                if (ans == 0) {
                    empty++
                } else if (ans == correctAns) {
                    correct++
                } else {
                    incorrect++
                }
            }
            
            val totalSecQ = range.count()
            val scoreSec = (correct * 1.0) + (incorrect * -0.33)
            val scoreSecPct = if (totalSecQ > 0) (scoreSec / totalSecQ.toDouble()) * 100.0 else 0.0
            val rawSecPct = if (totalSecQ > 0) (correct.toDouble() / totalSecQ.toDouble()) * 100.0 else 0.0

            SectionAnalyticData(
                sectionName = sec.name,
                totalQuestions = totalSecQ,
                correct = correct,
                incorrect = incorrect,
                empty = empty,
                rawPercentage = rawSecPct,
                negativeScore = scoreSec,
                negativePercentage = scoreSecPct,
                startQ = sec.startQuestion,
                endQ = sec.endQuestion
            )
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (hasKey) "Performance Scorecard" else "Session Saved",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackToDashboard,
                            modifier = Modifier.testTag("scorecard_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back"
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
            // Global Metrics Overview Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentExam.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (hasKey) {
                            // Large beautiful percentage circles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Standard Pct circle
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        modifier = Modifier.size(90.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${String.format("%.1f", currentAttempt.percentage)}%",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Standard Score",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Negative scoring Pct circle
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        modifier = Modifier.size(90.dp),
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${String.format("%.1f", currentAttempt.negativeScorePercentage)}%",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Negative Marking",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            // Score details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ScoreBreakdownItem(
                                    label = "Correct",
                                    count = currentAttempt.correctCount,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                                ScoreBreakdownItem(
                                    label = "Incorrect",
                                    count = currentAttempt.incorrectCount,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                ScoreBreakdownItem(
                                    label = "Unanswered",
                                    count = currentAttempt.unansweredCount,
                                    tint = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Negative Score calculated as: +1 (Correct) | -0.33 (Incorrect) | 0 (Blank). Raw score is ${String.format("%.2f", currentAttempt.negativeScore)} out of ${currentAttempt.totalQuestions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        } else {
                            // Bubble Sheet response overview with no grading key
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(90.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Bubble Sheet Recorded",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Practice responses have been saved completely. Add or import an Answer Key CSV in modifications to grade this exam later.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val answered = currentAttempt.totalQuestions - currentAttempt.unansweredCount
                                    ScoreBreakdownItem(
                                        label = "Total Questions",
                                        count = currentAttempt.totalQuestions,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    ScoreBreakdownItem(
                                        label = "Answered",
                                        count = answered,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.weight(1f)
                                    )
                                    ScoreBreakdownItem(
                                        label = "Blank",
                                        count = currentAttempt.unansweredCount,
                                        tint = Color.Gray,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Overall Answer Sheet Bubble Overview
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Answer Sheet Bubble Overview",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Unified Visual OMR Legend
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "OMR Grid Color Legend:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                LegendIndicatorItem(
                                    color = Color(0xFF2E7D32),
                                    bgColor = Color(0xFFE8F5E9),
                                    text = "Correct Answer",
                                    modifier = Modifier.weight(1f)
                                )
                                LegendIndicatorItem(
                                    color = Color(0xFFEF5350),
                                    bgColor = Color(0xFFFFEBEE),
                                    text = "Incorrect Answer",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                LegendIndicatorItem(
                                    color = Color(0xFF3F51B5),
                                    bgColor = Color(0xFFE8EAF6),
                                    text = "Answer Saved (No Key)",
                                    modifier = Modifier.weight(1f)
                                )
                                LegendIndicatorItem(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    text = "Blank / Skipped",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        val totalQ = currentExam.totalQuestions
                        val columnsCount = 6
                        val qNumsList = (1..totalQ).toList()
                        val questionChunks = qNumsList.chunked(columnsCount)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            questionChunks.forEach { rowQuestions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowQuestions.forEach { qNum ->
                                        val userSelection = attemptAnswers[qNum] ?: 0
                                        val correctSelection = if (hasKey) correctKeyAnswers[qNum] ?: 0 else 0
                                        
                                        val isCorrect = hasKey && userSelection == correctSelection
                                        val isUnanswered = userSelection == 0

                                        val userAnsChar = when (userSelection) {
                                            1 -> "A"
                                            2 -> "B"
                                            3 -> "C"
                                            4 -> "D"
                                            else -> "—"
                                        }

                                        val correctAnsChar = when (correctSelection) {
                                            1 -> "A"
                                            2 -> "B"
                                            3 -> "C"
                                            4 -> "D"
                                            else -> ""
                                        }

                                        val bubbleBgColor = when {
                                            isUnanswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            !hasKey -> Color(0xFFE8EAF6) // light Indigo/Blue filled look
                                            isCorrect -> Color(0xFFE8F5E9)
                                            else -> Color(0xFFFFEBEE)
                                        }

                                        val borderColor = when {
                                            isUnanswered -> MaterialTheme.colorScheme.outlineVariant
                                            !hasKey -> Color(0xFF3F51B5) // Indigo border
                                            isCorrect -> Color(0xFF2E7D32)
                                            else -> Color(0xFFEF5350)
                                        }

                                        val textColor = when {
                                            isUnanswered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            !hasKey -> Color(0xFF3F51B5) // Indigo text
                                            isCorrect -> Color(0xFF1B5E20)
                                            else -> Color(0xFFC62828)
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "$qNum",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(bubbleBgColor)
                                                    .border(BorderStroke(1.5.dp, borderColor), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = userAnsChar,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp,
                                                    color = textColor,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier.height(14.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (hasKey && !isCorrect && !isUnanswered) {
                                                    Text(
                                                        text = "Key: $correctAnsChar",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    // Fill up remaining spots if list does not complete column count
                                    val placeholders = columnsCount - rowQuestions.size
                                    repeat(placeholders) {
                                        Spacer(modifier = Modifier.width(44.dp).padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // CSV Export Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Export Practice Answers (CSV)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Save or copy your practiced bubble sheet responses in standard CSV format.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val context = LocalContext.current
                        var copiedMsg by remember { mutableStateOf(false) }

                        val csvText = remember(attemptAnswers, currentExam) {
                            val sheets = JsonSerializer.deserializeSections(currentExam.sectionsJson)
                            val sb = StringBuilder()
                            sb.append("Question,Answer,Section\n")
                            attemptAnswers.keys.sorted().forEach { qNum ->
                                val ans = attemptAnswers[qNum] ?: 0
                                val ansChar = when (ans) {
                                    1 -> "A"
                                    2 -> "B"
                                    3 -> "C"
                                    4 -> "D"
                                    else -> ""
                                }
                                val sectionName = sheets.find { qNum in it.startQuestion..it.endQuestion }?.name ?: ""
                                sb.append("$qNum,$ansChar,$sectionName\n")
                            }
                            sb.toString()
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Copy toclipboard
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("${currentExam.name} Answers", csvText)
                                    clipboard.setPrimaryClip(clip)
                                    copiedMsg = true
                                },
                                modifier = Modifier.weight(1f).testTag("copy_csv_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(if (copiedMsg) "Copied!" else "Copy CSV")
                            }

                            // Share via Intent
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "${currentExam.name} - Practiced Bubble Sheet")
                                        putExtra(Intent.EXTRA_TEXT, csvText)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Completed Answers"))
                                },
                                modifier = Modifier.weight(1f).testTag("share_csv_button")
                            ) {
                                Text("Share CSV")
                            }
                        }
                    }
                }
            }

            // Per-Section Analytics Header
            item {
                Text(
                    text = if (hasKey) "Section breakdown analytics" else "Section complete statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Per-Section analytics cards
            items(sectionsAnalytics) { secData ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = secData.sectionName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (hasKey) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Correct: ${String.format("%.1f", secData.rawPercentage)}%") }
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text("Neg: ${String.format("%.1f", secData.negativePercentage)}%") }
                                    )
                                }
                            } else {
                                val answeredSec = (secData.startQ..secData.endQ).count { attemptAnswers[it] ?: 0 != 0 }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("Filled: $answeredSec/${secData.totalQuestions}") }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (hasKey) {
                                Text(text = "Correct: ${secData.correct}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                Text(text = "Incorrect: ${secData.incorrect}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                Text(text = "Blank: ${secData.empty}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            } else {
                                val answeredSec = (secData.startQ..secData.endQ).count { attemptAnswers[it] ?: 0 != 0 }
                                val unansweredSec = secData.totalQuestions - answeredSec
                                Text(text = "Filled: $answeredSec", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                Text(text = "Blank: $unansweredSec", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "Items: ${secData.totalQuestions}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Comprehensive review list header
            item {
                Text(
                    text = if (hasKey) "Detailed Question Review" else "Detailed Saved Answers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Items list showcasing detailed selections
            val totalQCount = currentExam.totalQuestions
            val questions = if (hasKey) correctKeyAnswers.keys.sorted() else (1..totalQCount).toList()
            
            items(questions) { qNum ->
                val userSelection = attemptAnswers[qNum] ?: 0
                val correctSelection = if (hasKey) correctKeyAnswers[qNum] ?: 0 else 0

                val isCorrect = hasKey && userSelection == correctSelection
                val isUnanswered = userSelection == 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isUnanswered -> MaterialTheme.colorScheme.surface
                            !hasKey -> Color(0xFFE8EAF6) // Beautiful light Indigo/Blue filled look for Answer Saved without a key
                            isCorrect -> Color(0xFFE8F5E9)  // Soft light green
                            else -> Color(0xFFFFEBEE)       // Soft light red
                        }
                    ),
                    border = when {
                        isUnanswered -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        !hasKey -> BorderStroke(1.5.dp, Color(0xFF3F51B5))
                        else -> null
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Question $qNum",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Your Option: ${if (userSelection == 0) "Blank" else userSelection}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (hasKey) {
                                    Text(
                                        text = "•",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Correct Option: $correctSelection",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        // Right hand marker icon
                        if (hasKey) {
                            when {
                                isUnanswered -> {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Unanswered",
                                        tint = Color.Gray
                                    )
                                }
                                isCorrect -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Incorrect",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else {
                            if (!isUnanswered) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Saved Answer",
                                    tint = Color(0xFF3F51B5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Actions panel buttons
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("back_to_dashboard_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back to Dashboard", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.TakeExam(currentExam.id)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("retake_exam_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (hasKey) "Retake Exam" else "Record Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBreakdownItem(
    label: String,
    count: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

data class SectionAnalyticData(
    val sectionName: String,
    val totalQuestions: Int,
    val correct: Int,
    val incorrect: Int,
    val empty: Int,
    val rawPercentage: Double,
    val negativeScore: Double,
    val negativePercentage: Double,
    val startQ: Int,
    val endQ: Int
)

@Composable
fun LegendIndicatorItem(
    color: Color,
    bgColor: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(BorderStroke(1.2.dp, color), CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
