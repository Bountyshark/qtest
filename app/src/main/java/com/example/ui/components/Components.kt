package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.CorrectGreenLight
import com.example.ui.theme.IncorrectRed
import com.example.ui.theme.IncorrectRedLight
import com.example.ui.theme.SavedBlue
import com.example.ui.theme.SavedBlueLight

// ──────────────────────────────────────────────
// Top App Bar
// ──────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

// ──────────────────────────────────────────────
// Graded Card
// ──────────────────────────────────────────────

@Composable
fun GradedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = null,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val card = @Composable {
        Card(
            modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
    card()
}

// ──────────────────────────────────────────────
// Stat Chip
// ──────────────────────────────────────────────

enum class StatColor {
    Primary, Secondary, Tertiary, Error, Correct, Blank
}

@Composable
fun StatChip(
    count: Int,
    label: String,
    color: StatColor,
    modifier: Modifier = Modifier,
) {
    val chipColor = when (color) {
        StatColor.Primary -> MaterialTheme.colorScheme.primary
        StatColor.Secondary -> MaterialTheme.colorScheme.secondary
        StatColor.Tertiary -> MaterialTheme.colorScheme.tertiary
        StatColor.Error -> MaterialTheme.colorScheme.error
        StatColor.Correct -> Color(0xFF2E7D32)
        StatColor.Blank -> Color(0xFF9E9E9E)
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = chipColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

// ──────────────────────────────────────────────
// Draft Badge
// ──────────────────────────────────────────────

@Composable
fun DraftBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = modifier,
    ) {
        Text(
            text = "Draft",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

// ──────────────────────────────────────────────
// Confirm Dialog
// ──────────────────────────────────────────────

@Composable
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = if (isDestructive) {
                        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.textButtonColors()
                    },
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            },
        )
    }
}

// ──────────────────────────────────────────────
// Empty State
// ──────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Info,
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(actionLabel)
            }
        }
    }
}

// ──────────────────────────────────────────────
// Question Bubble (option selector 1-4)
// ──────────────────────────────────────────────

@Composable
fun QuestionBubble(
    optionNumber: Int,
    isSelected: Boolean,
    isAnyAnswered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    )
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isAnyAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
    )
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.onPrimary
            isAnyAnswered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$optionNumber",
            fontWeight = FontWeight.Black,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
        )
    }
}

// ──────────────────────────────────────────────
// Question Card (question + 4 bubbles)
// ──────────────────────────────────────────────

@Composable
fun QuestionCard(
    questionNumber: Int,
    selectedOption: Int,
    onOptionSelect: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAnswered = selectedOption != 0

    val cardBgColor by animateColorAsState(
        targetValue = if (isAnswered) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(350),
    )
    val cardBorderColor by animateColorAsState(
        targetValue = if (isAnswered) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(350),
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isAnswered) 0.35f else 1f,
        animationSpec = tween(350),
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_row_$questionNumber"),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(width = 1.dp, color = cardBorderColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Question $questionNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                )
                if (isAnswered) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("clear_button_$questionNumber"),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Answer",
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (opt in 1..4) {
                    QuestionBubble(
                        optionNumber = opt,
                        isSelected = selectedOption == opt,
                        isAnyAnswered = isAnswered,
                        onClick = { onOptionSelect(opt) },
                        modifier = Modifier.testTag("q_${questionNumber}_option_$opt"),
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// OMR Bubble Grid
// ──────────────────────────────────────────────

@Composable
fun OMRGrid(
    totalQuestions: Int,
    attemptAnswers: Map<Int, Int>,
    correctKeyAnswers: Map<Int, Int>,
    hasKey: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Answer Sheet Overview",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LegendDot(color = Color(0xFF2E7D32), label = "Correct")
                        LegendDot(color = MaterialTheme.colorScheme.error, label = "Incorrect")
                        if (!hasKey) {
                            LegendDot(color = Color(0xFF3F51B5), label = "Saved")
                        }
                        LegendDot(color = Color.Gray, label = "Blank")
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val columnsCount = 6
                    val chunks = (1..totalQuestions).toList().chunked(columnsCount)
                    chunks.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            row.forEach { qNum ->
                                OMRBubble(
                                    qNum = qNum,
                                    userAnswer = attemptAnswers[qNum] ?: 0,
                                    correctAnswer = if (hasKey) correctKeyAnswers[qNum] ?: 0 else 0,
                                    hasKey = hasKey,
                                )
                            }
                            repeat(columnsCount - row.size) {
                                Spacer(modifier = Modifier.width(36.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OMRBubble(
    qNum: Int,
    userAnswer: Int,
    correctAnswer: Int,
    hasKey: Boolean,
) {
    val isCorrect = hasKey && userAnswer == correctAnswer
    val isUnanswered = userAnswer == 0

    val bubbleBg = when {
        isUnanswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        !hasKey -> SavedBlueLight
        isCorrect -> CorrectGreenLight
        else -> IncorrectRedLight
    }
    val borderColor = when {
        isUnanswered -> MaterialTheme.colorScheme.outlineVariant
        !hasKey -> SavedBlue
        isCorrect -> CorrectGreen
        else -> IncorrectRed
    }
    val textColor = when {
        isUnanswered -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        !hasKey -> SavedBlue
        isCorrect -> Color(0xFF1B5E20)
        else -> Color(0xFFC62828)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(1.dp)) {
        Text(
            text = "$qNum",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
        )
        Spacer(modifier = Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(bubbleBg)
                .border(BorderStroke(1.dp, borderColor), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when (userAnswer) { 1 -> "A"; 2 -> "B"; 3 -> "C"; 4 -> "D"; else -> "—" },
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = textColor,
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Box(modifier = Modifier.height(12.dp), contentAlignment = Alignment.Center) {
            if (hasKey && !isCorrect && !isUnanswered) {
                Text(
                    text = "K:${when (correctAnswer) { 1 -> "A"; 2 -> "B"; 3 -> "C"; 4 -> "D"; else -> "" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CorrectGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// Timer Display
// ──────────────────────────────────────────────

@Composable
fun TimerDisplay(
    seconds: Long,
    modifier: Modifier = Modifier,
) {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    val formatted = String.format("%02d:%02d:%02d", hrs, mins, secs)
    val isDanger = seconds < 120

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isDanger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.padding(end = 8.dp),
    ) {
        Text(
            text = formatted,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("exam_timer_display"),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDanger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

// ──────────────────────────────────────────────
// Legend Dot
// ──────────────────────────────────────────────

@Composable
fun LegendDot(
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
    }
}
