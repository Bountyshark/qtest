package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExamSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamScreen(
    viewModel: ExamViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Temporary section addition states
    var secName by remember { mutableStateOf("") }
    var secStart by remember { mutableStateOf("") }
    var secEnd by remember { mutableStateOf("") }
    var sectionError by remember { mutableStateOf("") }

    // CSV Document picker
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val content = viewModel.readCsvFromUri(context, uri)
            if (content.isNotBlank()) {
                viewModel.createCsvInputText = content
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Create Practice Exam", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Configure Specifications & Answers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Exam Name
            OutlinedTextField(
                value = viewModel.createName,
                onValueChange = { viewModel.createName = it },
                label = { Text("Exam Name") },
                placeholder = { Text("e.g. JEE Main Mock Paper 1") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_exam_name_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Total Questions
            OutlinedTextField(
                value = viewModel.createTotalQuestionsText,
                onValueChange = { viewModel.createTotalQuestionsText = it },
                label = { Text("Total Number of Questions") },
                placeholder = { Text("e.g. 90") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_exam_questions_input"),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            // Optional Timer Settings
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = viewModel.createHasTimer,
                            onCheckedChange = { viewModel.createHasTimer = it },
                            modifier = Modifier.testTag("create_exam_timer_checkbox")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set Exam Time Limit",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (viewModel.createHasTimer) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.createTimerHours,
                                onValueChange = { viewModel.createTimerHours = it },
                                label = { Text("Hours") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_exam_hours_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                            OutlinedTextField(
                                value = viewModel.createTimerMinutes,
                                onValueChange = { viewModel.createTimerMinutes = it },
                                label = { Text("Mins") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_exam_minutes_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                            OutlinedTextField(
                                value = viewModel.createTimerSeconds,
                                onValueChange = { viewModel.createTimerSeconds = it },
                                label = { Text("Secs") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_exam_seconds_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }

            // Optional Sections definition card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Optional Exam Sections",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Divide your exam into sections (e.g. Physics, Chemistry). If left empty, sections will be auto-detected from the CSV key.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fields to add section
                    OutlinedTextField(
                        value = secName,
                        onValueChange = { secName = it },
                        label = { Text("Section Name") },
                        placeholder = { Text("e.g. Physics") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = secStart,
                            onValueChange = { secStart = it },
                            label = { Text("First Q (Start)") },
                            placeholder = { Text("e.g. 1") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                        OutlinedTextField(
                            value = secEnd,
                            onValueChange = { secEnd = it },
                            label = { Text("Last Q (End)") },
                            placeholder = { Text("e.g. 30") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (sectionError.isNotBlank()) {
                        Text(
                            text = sectionError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            sectionError = ""
                            if (secName.isBlank()) {
                                sectionError = "Section name cannot be blank"
                                return@Button
                            }
                            val start = secStart.toIntOrNull() ?: 0
                            val end = secEnd.toIntOrNull() ?: 0
                            if (start <= 0 || end <= 0 || start > end) {
                                sectionError = "Invalid Q ranges specified"
                                return@Button
                            }
                            viewModel.customSections.add(ExamSection(name = secName, startQuestion = start, endQuestion = end))
                            secName = ""
                            secStart = ""
                            secEnd = ""
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Section")
                    }

                    if (viewModel.customSections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Configured Sections:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        viewModel.customSections.forEachIndexed { i, section ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${section.name}: Q${section.startQuestion} - Q${section.endQuestion}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                IconButton(
                                    onClick = { viewModel.customSections.removeAt(i) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Answer Key CSV input card (Optional)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Upload Answer Key (CSV) - Optional",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upload a CSV file or paste answers below. If omitted, this exam acts as a response recorder (bubble-sheet tracker) allowing you to save your answers and export them to a CSV file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { csvPickerLauncher.launch("text/*") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("upload_csv_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Select CSV File")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.createCsvInputText,
                        onValueChange = { viewModel.createCsvInputText = it },
                        label = { Text("Paste Answer Key Content (CSV Format)") },
                        placeholder = {
                            Text(
                                "Format: QuestionNum,CorrectOption,SectionName(optional)\ne.g.\n1,A,Physics\n2,1,Physics\n3,C,Chemistry"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("csv_pasted_input"),
                        textStyle = MaterialTheme.typography.bodySmall,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Correct answers are parsed into Option 1, 2, 3, or 4. (Letters A, B, C, D automatically translate to options 1, 2, 3, 4).",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Error display
            if (viewModel.validationError.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = viewModel.validationError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Create submit button
            Button(
                onClick = { viewModel.submitCreateExam(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_exam_creation_button")
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create Practice Exam", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
