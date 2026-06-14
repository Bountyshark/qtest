package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exam
import com.example.data.ExamAttempt
import com.example.data.JsonSerializer
import com.example.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExamViewModel,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exams by viewModel.exams.collectAsState()
    val selectedExam by viewModel.selectedExam.collectAsState()
    val attempts by viewModel.attemptsOfSelectedExam.collectAsState()
    val draftExamIds by viewModel.draftExamIds.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (selectedExam != null) selectedExam!!.name else "Exam Grader & Tracker",
                onBack = if (selectedExam != null) {{ viewModel.navigateTo(Screen.Dashboard) }} else null,
                actions = {
                    if (selectedExam == null) {
                        IconButton(
                            onClick = { showMenu = !showMenu },
                            modifier = Modifier.testTag("dashboard_menu_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options Menu",
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("About Exam Grader") },
                                onClick = { showMenu = false; showAboutDialog = true },
                                modifier = Modifier.testTag("menu_about_app"),
                            )
                            DropdownMenuItem(
                                text = { Text("About OMR Sheets (Tips)") },
                                onClick = { showMenu = false; showHelpDialog = true },
                                modifier = Modifier.testTag("menu_about_omr"),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (selectedExam == null) {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    modifier = Modifier
                        .testTag("create_exam_fab")
                        .padding(bottom = 16.dp, end = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Exam")
                }
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (selectedExam == null) {
                if (exams.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.Info,
                        title = "No Exams Created",
                        body = "Create an exam, load an official CSV Answer Key, and start taking practice papers with negative scoring analytics.",
                        actionLabel = "Create Your First Exam",
                        onAction = onNavigateToCreate,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            Text(
                                text = "Your Practice Papers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        items(exams) { exam ->
                            ExamCard(
                                exam = exam,
                                hasDraft = exam.id in draftExamIds,
                                onClick = { viewModel.navigateTo(Screen.ExamDetail(exam.id)) },
                                onContinue = { viewModel.navigateTo(Screen.TakeExam(exam.id)) },
                                onDelete = { showDeleteConfirm = exam.id },
                            )
                        }
                    }
                }
            } else {
                ExamDetailContent(
                    exam = selectedExam!!,
                    attempts = attempts,
                    hasDraft = selectedExam!!.id in draftExamIds,
                    onBack = { viewModel.navigateTo(Screen.Dashboard) },
                    onTakeExam = { viewModel.navigateTo(Screen.TakeExam(selectedExam!!.id)) },
                    onStartFresh = { viewModel.startFreshExam(selectedExam!!.id) },
                    onViewAttempt = { attemptId ->
                        viewModel.navigateTo(Screen.ExamResult(selectedExam!!.id, attemptId))
                    },
                    onDeleteAttempt = { attemptId ->
                        viewModel.deleteAttempt(attemptId, selectedExam!!.id)
                    },
                )
            }

            ConfirmDialog(
                visible = showDeleteConfirm != null,
                title = "Delete Exam?",
                message = "Are you sure you want to delete this exam and all of its attempt history? This action is irreversible.",
                confirmText = "Delete",
                isDestructive = true,
                onConfirm = {
                    showDeleteConfirm?.let { viewModel.deleteExam(it) }
                    showDeleteConfirm = null
                },
                onDismiss = { showDeleteConfirm = null },
            )

            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    title = { Text("About Exam Grader") },
                    text = {
                        Text(
                            "This application acts as a high-fidelity Bubble Sheet OMR tracker and exam optimizer, designed for students preparing for examinations with negative marking systems.\n\n" +
                            "Version: 1.2.0\n" +
                            "Built with Jetpack Compose & Material 3 Design."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("Close")
                        }
                    },
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = { Text("About OMR & Grading Sheets") },
                    text = {
                        Text(
                            "A standard OMR (Optical Mark Recognition) grading sheet operates with multiple-choice bubbles (1 to 4 or A to D).\n\n" +
                            "Tips:\n" +
                            "• Dynamic Sections: Organize exams into distinct subjects (e.g. Physics, Chemistry).\n" +
                            "• Optional Answer Key: You can record responses first, and paste or upload an answer key later to get instant grades with a university negative-scoring analytical factor (-0.33 per wrong response)."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Got it")
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun ExamCard(
    exam: Exam,
    hasDraft: Boolean = false,
    onClick: () -> Unit,
    onContinue: () -> Unit = {},
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = exam.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (hasDraft) {
                    Spacer(modifier = Modifier.width(8.dp))
                    DraftBadge()
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "${exam.totalQuestions} Questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
                val durationStr = if (exam.timerSeconds != null) {
                    val hours = exam.timerSeconds / 3600
                    val mins = (exam.timerSeconds % 3600) / 60
                    if (hours > 0) "${hours}h ${mins}m" else "${mins} mins"
                } else {
                    "Untimed"
                }
                Text(
                    text = durationStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
            if (hasDraft) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("continue_exam_button_${exam.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                    ),
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_exam_button_${exam.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Exam",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun ExamDetailContent(
    exam: Exam,
    attempts: List<ExamAttempt>,
    hasDraft: Boolean = false,
    onBack: () -> Unit,
    onTakeExam: () -> Unit,
    onStartFresh: () -> Unit = {},
    onViewAttempt: (Long) -> Unit,
    onDeleteAttempt: (Long) -> Unit,
) {
    var showDeleteAttemptConfirm by remember { mutableStateOf<Long?>(null) }
    val sections = remember(exam.sectionsJson) {
        JsonSerializer.deserializeSections(exam.sectionsJson)
    }
    val hasKey = remember(exam) {
        exam.answersJson.isNotBlank() && exam.answersJson != "{}"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = exam.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Created ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(exam.createdAt))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }

        item {
            GradedCard(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "Exam Specifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "${exam.totalQuestions}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column {
                        Text(
                            text = "Sections",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                        Text(
                            text = "${sections.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column {
                        Text(
                            text = "Timer limit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        )
                        val limitStr = exam.timerSeconds?.let {
                            val hrs = it / 3600
                            val mins = (it % 3600) / 60
                            if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                        } ?: "No Limit"
                        Text(
                            text = limitStr,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (sections.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sections range:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    )
                    sections.forEach { section ->
                        Text(
                            text = "  ${section.name}: Q${section.startQuestion} - Q${section.endQuestion}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        item {
            if (hasDraft) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onTakeExam,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("continue_practice_button")
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                        ),
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue Practice", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    OutlinedButton(
                        onClick = onStartFresh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_fresh_button")
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text("Start New Attempt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                Button(
                    onClick = onTakeExam,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_exam_button")
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Practice Attempt", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (hasKey && attempts.size >= 1) {
            item {
                GradedCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        text = "Performance Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Tracking your score improvement over successive attempts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AttemptsChart(attempts = attempts.reversed())
                }
            }
        }

        item {
            Text(
                text = "Previous Attempts (${attempts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (attempts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No practice attempts made yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(attempts) { attempt ->
                AttemptItemRow(
                    attempt = attempt,
                    hasKey = hasKey,
                    onView = { onViewAttempt(attempt.id) },
                    onDelete = { showDeleteAttemptConfirm = attempt.id },
                )
            }
        }
    }

    ConfirmDialog(
        visible = showDeleteAttemptConfirm != null,
        title = "Delete Attempt?",
        message = "Are you sure you want to delete this practice attempt's historic log? This will remove it from the charts.",
        confirmText = "Delete",
        isDestructive = true,
        onConfirm = {
            showDeleteAttemptConfirm?.let { onDeleteAttempt(it) }
            showDeleteAttemptConfirm = null
        },
        onDismiss = { showDeleteAttemptConfirm = null },
    )
}

@Composable
fun AttemptItemRow(
    attempt: ExamAttempt,
    hasKey: Boolean,
    onView: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() }
            .testTag("attempt_row_${attempt.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
                        .format(Date(attempt.createdAt)),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (hasKey) {
                        Text(
                            text = "Score: ${String.format("%.1f", attempt.percentage)}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Neg: ${String.format("%.2f", attempt.negativeScore)} (${String.format("%.1f", attempt.negativeScorePercentage)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    } else {
                        val answered = attempt.totalQuestions - attempt.unansweredCount
                        Text(
                            text = "Answered: $answered/${attempt.totalQuestions}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Recorded Sheet",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    Text(
                        text = "${attempt.timeSpentSeconds / 60}m ${attempt.timeSpentSeconds % 60}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Attempt",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
fun AttemptsChart(
    attempts: List<ExamAttempt>,
    modifier: Modifier = Modifier,
) {
    if (attempts.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
    ) {
        val strokeColor = MaterialTheme.colorScheme.primary
        val negStrokeColor = MaterialTheme.colorScheme.secondary
        val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val padding = 20.dp.toPx()
            val chartWidth = width - (padding * 2)
            val chartHeight = height - (padding * 2)

            val maxPointsCount = maxOf(2, attempts.size)
            val dx = chartWidth / (maxPointsCount - 1).coerceAtLeast(1)

            for (i in 0..4) {
                val yVal = padding + (chartHeight * (i / 4f))
                drawLine(
                    color = gridColor,
                    start = Offset(padding, yVal),
                    end = Offset(width - padding, yVal),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val scorePairs = attempts.mapIndexed { index, attempt ->
                index.toFloat() to attempt.percentage.toFloat()
            }
            val negPairs = attempts.mapIndexed { index, attempt ->
                index.toFloat() to attempt.negativeScorePercentage.toFloat()
            }

            if (scorePairs.size > 1) {
                val scorePath = Path().apply {
                    scorePairs.forEachIndexed { idx, pair ->
                        val x = padding + (idx * dx)
                        val y = padding + chartHeight * (1f - (pair.second / 100f))
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = scorePath,
                    color = strokeColor,
                    style = Stroke(width = 3.dp.toPx()),
                )
            }

            if (negPairs.size > 1) {
                val negPath = Path().apply {
                    negPairs.forEachIndexed { idx, pair ->
                        val x = padding + (idx * dx)
                        val normalizedScore = ((pair.second + 33f) / 133f).coerceIn(0f, 1f)
                        val y = padding + chartHeight * (1f - normalizedScore)
                        if (idx == 0) moveTo(x, y) else lineTo(x, y)
                    }
                }
                drawPath(
                    path = negPath,
                    color = negStrokeColor,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            attempts.forEachIndexed { idx, attempt ->
                val x = padding + (idx * dx)
                val yScore = padding + chartHeight * (1f - (attempt.percentage.toFloat() / 100f))
                drawCircle(
                    color = strokeColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, yScore),
                )
                val normNeg = ((attempt.negativeScorePercentage.toFloat() + 33f) / 133f).coerceIn(0f, 1f)
                val yNeg = padding + chartHeight * (1f - normNeg)
                drawCircle(
                    color = negStrokeColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, yNeg),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Correct %", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Neg. Scored %", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
            }
            Text(
                text = "Attempts (L to R)",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
