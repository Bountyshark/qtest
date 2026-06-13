package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class Screen {
    object Dashboard : Screen()
    object CreateExam : Screen()
    data class ExamDetail(val examId: Long) : Screen()
    data class TakeExam(val examId: Long) : Screen()
    data class ExamResult(val examId: Long, val attemptId: Long) : Screen()
}

data class ParsedRow(
    val qNum: Int,
    val correctOpt: Int,
    val sectionName: String?
)

class ExamViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ExamDatabase.getDatabase(application)
    private val repository = ExamRepository(database.examDao())

    var currentScreen by mutableStateOf<Screen>(Screen.Dashboard)
        private set

    val exams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedExam = MutableStateFlow<Exam?>(null)
    val selectedExam: StateFlow<Exam?> = _selectedExam.asStateFlow()

    private val _attemptsOfSelectedExam = MutableStateFlow<List<ExamAttempt>>(emptyList())
    val attemptsOfSelectedExam: StateFlow<List<ExamAttempt>> = _attemptsOfSelectedExam.asStateFlow()

    private val _selectedAttempt = MutableStateFlow<ExamAttempt?>(null)
    val selectedAttempt: StateFlow<ExamAttempt?> = _selectedAttempt.asStateFlow()

    val draftExamIds: StateFlow<Set<Long>> = repository.getDraftExamIds()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // ----------------------------------------------------
    // EXAM CREATION FORM STATE
    // ----------------------------------------------------
    var createName by mutableStateOf("")
    var createTotalQuestionsText by mutableStateOf("")
    var createHasTimer by mutableStateOf(false)
    var createTimerHours by mutableStateOf("0")
    var createTimerMinutes by mutableStateOf("30")
    var createTimerSeconds by mutableStateOf("00")
    var createCsvInputText by mutableStateOf("")
    var csvError by mutableStateOf("")
    var customSections = mutableListOf<ExamSection>()
    var validationError by mutableStateOf("")

    // ----------------------------------------------------
    // EXAM TAKING STATE
    // ----------------------------------------------------
    var activeExamId by mutableStateOf<Long?>(null)
    val activeAnswers = androidx.compose.runtime.mutableStateMapOf<Int, Int>()
    var currentSectionIndex by mutableStateOf(0)
    var activeSectionsList by mutableStateOf<List<ExamSection>>(emptyList())
    var timeRemainingSeconds by mutableStateOf<Long>(0L)
    var activeTimeLimitSeconds by mutableStateOf<Long?>(null)
    var activeTimeSpentSeconds by mutableStateOf<Long>(0L)
    var activeDraftId by mutableStateOf<Long?>(null)
        private set

    private var timerJob: Job? = null
    private var timerStartTimeMs = 0L
    private var draftLoaded = false
    private var autoSaveJob: Job? = null

    // ----------------------------------------------------
    // ACTIONS
    // ----------------------------------------------------
    fun navigateTo(screen: Screen) {
        currentScreen = screen
        when (screen) {
            is Screen.Dashboard -> {
                _selectedExam.value = null
                _attemptsOfSelectedExam.value = emptyList()
                _selectedAttempt.value = null
            }
            is Screen.CreateExam -> {
                clearCreationForm()
            }
            is Screen.ExamDetail -> {
                loadExamDetails(screen.examId)
            }
            is Screen.TakeExam -> {
                startExam(screen.examId)
            }
            is Screen.ExamResult -> {
                loadAttemptDetails(screen.examId, screen.attemptId)
            }
        }
    }

    private fun loadExamDetails(examId: Long) {
        viewModelScope.launch {
            val exam = repository.getExamById(examId)
            _selectedExam.value = exam
            if (exam != null) {
                repository.getAttemptsForExam(examId).collectLatest { attempts ->
                    _attemptsOfSelectedExam.value = attempts
                }
            }
        }
    }

    private fun loadAttemptDetails(examId: Long, attemptId: Long) {
        viewModelScope.launch {
            val exam = repository.getExamById(examId)
            _selectedExam.value = exam
            repository.getAttemptsForExam(examId).collectLatest { attempts ->
                _attemptsOfSelectedExam.value = attempts
                _selectedAttempt.value = attempts.find { it.id == attemptId }
            }
        }
    }

    fun deleteExam(examId: Long) {
        viewModelScope.launch {
            repository.deleteExam(examId)
            navigateTo(Screen.Dashboard)
        }
    }
    
    fun deleteAttempt(attemptId: Long, examId: Long) {
        viewModelScope.launch {
            repository.deleteAttemptById(attemptId)
            loadExamDetails(examId)
        }
    }

    // ----------------------------------------------------
    // DRAFT / AUTO-SAVE LOGIC
    // ----------------------------------------------------
    fun saveDraft() {
        val examId = activeExamId ?: return
        val currentExam = selectedExam.value ?: return

        viewModelScope.launch {
            val attempt = ExamAttempt(
                id = activeDraftId ?: 0,
                examId = examId,
                answersJson = JsonSerializer.serializeMap(activeAnswers),
                timeSpentSeconds = activeTimeSpentSeconds,
                totalQuestions = currentExam.totalQuestions,
                correctCount = 0,
                incorrectCount = 0,
                unansweredCount = 0,
                percentage = 0.0,
                negativeScore = 0.0,
                negativeScorePercentage = 0.0,
                isDraft = true
            )
            val savedId = repository.insertAttempt(attempt)
            activeDraftId = savedId
        }
    }

    fun saveAndExit() {
        saveDraft()
        val examId = activeExamId ?: return
        timerJob?.cancel()
        timerJob = null
        autoSaveJob?.cancel()
        autoSaveJob = null
        navigateTo(Screen.ExamDetail(examId))
    }

    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            snapshotFlow { activeAnswers.toMap() }
                .collectLatest {
                    if (draftLoaded) {
                        delay(500)
                        saveDraft()
                    }
                }
        }
    }

    // ----------------------------------------------------
    // EXAM CREATION LOGIC
    // ----------------------------------------------------
    fun clearCreationForm() {
        createName = ""
        createTotalQuestionsText = ""
        createHasTimer = false
        createTimerHours = "0"
        createTimerMinutes = "30"
        createTimerSeconds = "00"
        createCsvInputText = ""
        csvError = ""
        validationError = ""
        customSections.clear()
    }

    fun processCsvText(text: String): List<ParsedRow> {
        val rows = mutableListOf<ParsedRow>()
        val lines = text.split("\n", "\r")
        for (line in lines) {
            val tokens = line.split(",")
            if (tokens.size < 2) continue
            
            val rawQ = tokens[0].trim()
            val rawA = tokens[1].trim()
            val sectionVal = if (tokens.size >= 3) tokens[2].trim() else null

            val numStr = rawQ.filter { it.isDigit() }
            val qNum = numStr.toIntOrNull() ?: continue
            
            val correctOpt = parseOptionChar(rawA) ?: continue

            rows.add(ParsedRow(qNum = qNum, correctOpt = correctOpt, sectionName = sectionVal))
        }
        return rows
    }

    private fun parseOptionChar(str: String): Int? {
        val clean = str.trim().uppercase()
        return when (clean) {
            "1", "A" -> 1
            "2", "B" -> 2
            "3", "C" -> 3
            "4", "D" -> 4
            else -> null
        }
    }

    fun readCsvFromUri(context: Context, uri: Uri): String {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            inputStream?.close()
            sb.toString()
        } catch (e: Exception) {
            Log.e("ExamViewModel", "CSV Read failed", e)
            ""
        }
    }

    fun submitCreateExam(context: Context) {
        validationError = ""
        if (createName.isBlank()) {
            validationError = "Please enter an Exam Name"
            return
        }
        val totalQuestions = createTotalQuestionsText.toIntOrNull() ?: 0
        if (totalQuestions <= 0) {
            validationError = "Please enter a valid number of questions"
            return
        }

        val answersMap = mutableMapOf<Int, Int>()
        val csvSectionsMap = mutableMapOf<String, MutableList<Int>>()
        var actualTotalQuestions = totalQuestions

        if (createCsvInputText.isNotBlank()) {
            val parsedRows = processCsvText(createCsvInputText)
            if (parsedRows.isEmpty()) {
                validationError = "Failed to parse any valid questions/answers from CSV. Ensure column 1 has question numbers and column 2 has answers (e.g. 1,A or 1,1)."
                return
            }

            parsedRows.forEach { row ->
                answersMap[row.qNum] = row.correctOpt
                if (!row.sectionName.isNullOrBlank()) {
                    val list = csvSectionsMap.getOrPut(row.sectionName) { mutableListOf() }
                    list.add(row.qNum)
                }
            }

            val maxCsvQuestion = answersMap.keys.maxOrNull() ?: 0
            actualTotalQuestions = maxOf(totalQuestions, maxCsvQuestion)
        }

        val finalSections = mutableListOf<ExamSection>()
        if (customSections.isNotEmpty()) {
            finalSections.addAll(customSections)
        } else if (csvSectionsMap.isNotEmpty()) {
            csvSectionsMap.forEach { (secName, qList) ->
                val start = qList.minOrNull() ?: 1
                val end = qList.maxOrNull() ?: 1
                finalSections.add(ExamSection(name = secName, startQuestion = start, endQuestion = end))
            }
        }

        if (finalSections.isEmpty()) {
            finalSections.add(ExamSection(name = "General", startQuestion = 1, endQuestion = actualTotalQuestions))
        } else {
            finalSections.sortBy { it.startQuestion }
        }

        val sectionsJson = JsonSerializer.serializeSections(finalSections)
        val answersJson = JsonSerializer.serializeMap(answersMap)

        var totalTimerSecs: Long? = null
        if (createHasTimer) {
            val hours = createTimerHours.toLongOrNull() ?: 0L
            val mins = createTimerMinutes.toLongOrNull() ?: 0L
            val secs = createTimerSeconds.toLongOrNull() ?: 0L
            totalTimerSecs = (hours * 3600) + (mins * 60) + secs
            if (totalTimerSecs <= 0L) {
                validationError = "Timer enabled, but duration is 0. Please set a valid time limit."
                return
            }
        }

        val exam = Exam(
            name = createName,
            totalQuestions = actualTotalQuestions,
            timerSeconds = totalTimerSecs,
            sectionsJson = sectionsJson,
            answersJson = answersJson
        )

        viewModelScope.launch {
            val examId = repository.insertExam(exam)
            clearCreationForm()
            navigateTo(Screen.ExamDetail(examId))
        }
    }

    // ----------------------------------------------------
    // EXAM TAKING LOGIC
    // ----------------------------------------------------
    private fun startExam(examId: Long) {
        timerJob?.cancel()
        autoSaveJob?.cancel()
        activeExamId = examId
        activeDraftId = null
        draftLoaded = false
        activeAnswers.clear()
        currentSectionIndex = 0

        viewModelScope.launch {
            val exam = repository.getExamById(examId) ?: return@launch
            _selectedExam.value = exam
            activeSectionsList = JsonSerializer.deserializeSections(exam.sectionsJson)

            // Check for existing draft
            val draft = repository.getDraftAttempt(examId)
            if (draft != null) {
                val draftAnswers = JsonSerializer.deserializeMap(draft.answersJson)
                activeAnswers.clear()
                draftAnswers.forEach { (q, ans) -> activeAnswers[q] = ans }
                // Fill in any missing questions
                for (q in 1..exam.totalQuestions) {
                    if (q !in activeAnswers) activeAnswers[q] = 0
                }
                activeDraftId = draft.id
                activeTimeSpentSeconds = draft.timeSpentSeconds
            } else {
                for (q in 1..exam.totalQuestions) {
                    activeAnswers[q] = 0
                }
                activeTimeSpentSeconds = 0L
            }

            draftLoaded = true
            startAutoSave()

            activeTimeLimitSeconds = exam.timerSeconds

            if (exam.timerSeconds != null && exam.timerSeconds > 0L) {
                val elapsed = activeTimeSpentSeconds
                val remaining = maxOf(0L, exam.timerSeconds - elapsed)
                timeRemainingSeconds = remaining
                timerStartTimeMs = System.currentTimeMillis() - (elapsed * 1000)
                val targetEndTimeMs = timerStartTimeMs + (exam.timerSeconds * 1000)
                
                timerJob = viewModelScope.launch {
                    while (System.currentTimeMillis() < targetEndTimeMs) {
                        val currentMs = System.currentTimeMillis()
                        timeRemainingSeconds = maxOf(0L, (targetEndTimeMs - currentMs) / 1000)
                        activeTimeSpentSeconds = (currentMs - timerStartTimeMs) / 1000
                        delay(500)
                        
                        if (timeRemainingSeconds <= 0L) {
                            submitExamAnswers()
                            break
                        }
                    }
                }
            } else {
                timerStartTimeMs = System.currentTimeMillis() - (activeTimeSpentSeconds * 1000)
                timerJob = viewModelScope.launch {
                    while (true) {
                        activeTimeSpentSeconds = (System.currentTimeMillis() - timerStartTimeMs) / 1000
                        delay(1000)
                    }
                }
            }
        }
    }

    fun submitExamAnswers() {
        timerJob?.cancel()
        timerJob = null
        autoSaveJob?.cancel()
        autoSaveJob = null

        val examId = activeExamId ?: return
        val currentExam = selectedExam.value ?: return

        viewModelScope.launch {
            val correctKeyMap = JsonSerializer.deserializeMap(currentExam.answersJson)
            
            var correctCount = 0
            var incorrectCount = 0
            var unansweredCount = 0

            for (q in 1..currentExam.totalQuestions) {
                val userAns = activeAnswers[q] ?: 0
                val correctAns = correctKeyMap[q] ?: 0

                if (userAns == 0) {
                    unansweredCount++
                } else if (userAns == correctAns) {
                    correctCount++
                } else {
                    incorrectCount++
                }
            }

            val percentage = if (currentExam.totalQuestions > 0) {
                (correctCount.toDouble() / currentExam.totalQuestions.toDouble()) * 100.0
            } else {
                0.0
            }

            val rawNegativeScore = (correctCount * 1.0) + (incorrectCount * -0.33)
            val negativeScorePercentage = if (currentExam.totalQuestions > 0) {
                (rawNegativeScore / currentExam.totalQuestions.toDouble()) * 100.0
            } else {
                0.0
            }

            val attempt = ExamAttempt(
                examId = examId,
                answersJson = JsonSerializer.serializeMap(activeAnswers),
                timeSpentSeconds = activeTimeSpentSeconds,
                totalQuestions = currentExam.totalQuestions,
                correctCount = correctCount,
                incorrectCount = incorrectCount,
                unansweredCount = unansweredCount,
                percentage = percentage,
                negativeScore = rawNegativeScore,
                negativeScorePercentage = negativeScorePercentage,
                isDraft = false
            )

            val attemptId = repository.insertAttempt(attempt)

            // Delete the draft if it exists
            repository.deleteDraftAttempt(examId)
            activeDraftId = null

            navigateTo(Screen.ExamResult(examId, attemptId))
        }
    }

    fun startFreshExam(examId: Long) {
        viewModelScope.launch {
            repository.deleteDraftAttempt(examId)
            navigateTo(Screen.TakeExam(examId))
        }
    }
}
