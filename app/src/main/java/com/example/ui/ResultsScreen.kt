package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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

    var omrExpanded by remember { mutableStateOf(false) }
    val expandedSections = remember { mutableStateMapOf<Int, Boolean>() }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = if (hasKey) "Scorecard" else "Saved",
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
                    actions = {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("${currentExam.name} Answers", csvText)
                                clipboard.setPrimaryClip(clip)
                                copiedMsg = true
                            },
                            modifier = Modifier.testTag("copy_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy CSV",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "${currentExam.name} - Practiced Bubble Sheet")
                                    putExtra(Intent.EXTRA_TEXT, csvText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Completed Answers"))
                            },
                            modifier = Modifier.testTag("share_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share CSV",
                                tint = MaterialTheme.colorScheme.primary
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Compact Score Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentExam.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (hasKey) {
                            // Compact score chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            "${String.format("%.1f", currentAttempt.percentage)}%",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            "Neg: ${String.format("%.1f", currentAttempt.negativeScorePercentage)}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Inline stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CompactStatItem(label = "Correct", count = currentAttempt.correctCount, tint = Color(0xFF2E7D32))
                                CompactStatItem(label = "Incorrect", count = currentAttempt.incorrectCount, tint = MaterialTheme.colorScheme.error)
                                CompactStatItem(label = "Blank", count = currentAttempt.unansweredCount, tint = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+1 (Correct) | -0.33 (Incorrect) | 0 (Blank) — Raw: ${String.format("%.2f", currentAttempt.negativeScore)}/${currentAttempt.totalQuestions}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                fontSize = 9.sp
                            )
                        } else {
                            // No key — compact saved summary
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Bubble Sheet Recorded",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Responses saved. Add an Answer Key to grade later.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val answered = currentAttempt.totalQuestions - currentAttempt.unansweredCount
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CompactStatItem(label = "Total", count = currentAttempt.totalQuestions, tint = MaterialTheme.colorScheme.primary)
                                CompactStatItem(label = "Answered", count = answered, tint = Color(0xFF2E7D32))
                                CompactStatItem(label = "Blank", count = currentAttempt.unansweredCount, tint = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Collapsible OMR Bubble Grid
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
                            .clickable { omrExpanded = !omrExpanded }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Answer Sheet Overview",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Inline legend dots
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    LegendDot(color = Color(0xFF2E7D32), label = "Correct")
                                    LegendDot(color = MaterialTheme.colorScheme.error, label = "Incorrect")
                                    if (hasKey) {
                                        LegendDot(color = Color(0xFF3F51B5), label = "Saved")
                                    }
                                    LegendDot(color = Color.Gray, label = "Blank")
                                }
                            }
                            Icon(
                                imageVector = if (omrExpanded) Icons.AutoMirrored.Filled.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowDown,
                                contentDescription = if (omrExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = omrExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                val totalQ = currentExam.totalQuestions
                                val columnsCount = 6
                                val qNumsList = (1..totalQ).toList()
                                val questionChunks = qNumsList.chunked(columnsCount)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                                    !hasKey -> Color(0xFFE8EAF6)
                                                    isCorrect -> Color(0xFFE8F5E9)
                                                    else -> Color(0xFFFFEBEE)
                                                }

                                                val borderColor = when {
                                                    isUnanswered -> MaterialTheme.colorScheme.outlineVariant
                                                    !hasKey -> Color(0xFF3F51B5)
                                                    isCorrect -> Color(0xFF2E7D32)
                                                    else -> Color(0xFFEF5350)
                                                }

                                                val textColor = when {
                                                    isUnanswered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                    !hasKey -> Color(0xFF3F51B5)
                                                    isCorrect -> Color(0xFF1B5E20)
                                                    else -> Color(0xFFC62828)
                                                }

                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(1.dp)
                                                ) {
                                                    Text(
                                                        text = "$qNum",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(30.dp)
                                                            .clip(CircleShape)
                                                            .background(bubbleBgColor)
                                                            .border(BorderStroke(1.dp, borderColor), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = userAnsChar,
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 11.sp,
                                                            color = textColor,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(1.dp))
                                                    Box(
                                                        modifier = Modifier.height(12.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (hasKey && !isCorrect && !isUnanswered) {
                                                            Text(
                                                                text = "K:$correctAnsChar",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color(0xFF2E7D32),
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 8.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            val placeholders = columnsCount - rowQuestions.size
                                            repeat(placeholders) {
                                                Spacer(modifier = Modifier.width(36.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Section Analytics + Question Review
            item {
                Text(
                    text = if (hasKey) "Section Breakdown" else "Section Statistics",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(sectionsAnalytics.size) { index ->
                val secData = sectionsAnalytics[index]
                val isExpanded = expandedSections[index] == true

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column {
                        // Section header — always visible, clickable
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedSections[index] = !isExpanded }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = secData.sectionName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (hasKey) {
                                        Text(
                                            text = "C:${secData.correct}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "W:${secData.incorrect}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "B:${secData.empty}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    } else {
                                        val answeredSec = (secData.startQ..secData.endQ).count { attemptAnswers[it] ?: 0 != 0 }
                                        Text(
                                            text = "Filled: $answeredSec/${secData.totalQuestions}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "of ${secData.totalQuestions}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hasKey) {
                                    Text(
                                        text = "${String.format("%.0f", secData.rawPercentage)}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.AutoMirrored.Filled.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Expanded — show questions inline
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                val range = secData.startQ..secData.endQ
                                range.forEach { qNum ->
                                    val userSelection = attemptAnswers[qNum] ?: 0
                                    val correctSelection = if (hasKey) correctKeyAnswers[qNum] ?: 0 else 0
                                    val isCorrect = hasKey && userSelection == correctSelection
                                    val isUnanswered = userSelection == 0

                                    val userChar = when (userSelection) { 1 -> "A"; 2 -> "B"; 3 -> "C"; 4 -> "D"; else -> "—" }
                                    val correctChar = when (correctSelection) { 1 -> "A"; 2 -> "B"; 3 -> "C"; 4 -> "D"; else -> "" }

                                    val statusIcon = when {
                                        !hasKey && !isUnanswered -> Icons.Default.Check
                                        isUnanswered -> Icons.Default.Info
                                        isCorrect -> Icons.Default.Check
                                        else -> Icons.Default.Close
                                    }
                                    val statusTint = when {
                                        !hasKey && !isUnanswered -> Color(0xFF3F51B5)
                                        isUnanswered -> Color.Gray
                                        isCorrect -> Color(0xFF2E7D32)
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                    val rowBg = when {
                                        !hasKey && !isUnanswered -> Color(0xFFE8EAF6).copy(alpha = 0.5f)
                                        isUnanswered -> Color.Transparent
                                        isCorrect -> Color(0xFFE8F5E9).copy(alpha = 0.5f)
                                        else -> Color(0xFFFFEBEE).copy(alpha = 0.5f)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(rowBg, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Q$qNum",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.width(28.dp)
                                            )
                                            Text(
                                                text = userChar,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = statusTint
                                            )
                                            if (hasKey && !isUnanswered) {
                                                Text(
                                                    text = if (isCorrect) "✓" else "→$correctChar",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = statusTint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Action buttons
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("back_to_dashboard_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Dashboard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(Screen.TakeExam(currentExam.id)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("retake_exam_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (hasKey) "Retake" else "Record Again", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CompactStatItem(
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
            fontSize = 16.sp,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
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
fun LegendDot(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}
