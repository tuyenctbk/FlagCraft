package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FlagCraftDatabase
import com.example.data.local.QuizHistoryEntity
import com.example.data.model.Country
import com.example.data.repository.CountryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    HOME,
    FLASHCARD,
    BLITZ_QUIZ,
    PARTY_MODE,
    EXPLORER,
    STATS
}

enum class QuestionType {
    FLAG_TO_NAME,
    NAME_TO_FLAG,
    CAPITAL_TO_COUNTRY
}

data class QuizOption(
    val id: String,
    val text: String,
    val flagPng: String = "",
    val isCorrect: Boolean
)

data class QuizQuestion(
    val country: Country,
    val questionType: QuestionType,
    val promptText: String,
    val options: List<QuizOption>,
    val correctIndex: Int
)

data class BlitzState(
    val isRunning: Boolean = false,
    val score: Int = 0,
    val streak: Int = 0,
    val maxStreak: Int = 0,
    val totalAnswered: Int = 0,
    val correctAnswers: Int = 0,
    val timeLeftSeconds: Int = 60,
    val currentQuestion: QuizQuestion? = null,
    val isGameOver: Boolean = false,
    val selectedOptionIndex: Int? = null,
    val isCorrectSelection: Boolean? = null
)

data class PartyState(
    val playerCount: Int = 2,
    val playerNames: List<String> = listOf("Player 1", "Player 2", "Player 3", "Player 4"),
    val playerScores: MutableList<Int> = mutableListOf(0, 0, 0, 0),
    val currentPlayerTurn: Int = 0,
    val currentRound: Int = 1,
    val totalRounds: Int = 10,
    val currentQuestion: QuizQuestion? = null,
    val selectedAnswerIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val isGameOver: Boolean = false,
    val winnerName: String = "",
    val isStarted: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FlagCraftDatabase.getDatabase(application)
    private val repository = CountryRepository(db.countryDao(), db.quizHistoryDao())

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isTvPartyMode = MutableStateFlow(false)
    val isTvPartyMode: StateFlow<Boolean> = _isTvPartyMode.asStateFlow()

    private val _allCountries = MutableStateFlow<List<Country>>(emptyList())
    val allCountries: StateFlow<List<Country>> = _allCountries.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRegion = MutableStateFlow("All")
    val selectedRegion: StateFlow<String> = _selectedRegion.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    private val _filteredCountries = MutableStateFlow<List<Country>>(emptyList())
    val filteredCountries: StateFlow<List<Country>> = _filteredCountries.asStateFlow()

    private val _blitzState = MutableStateFlow(BlitzState())
    val blitzState: StateFlow<BlitzState> = _blitzState.asStateFlow()

    private val _partyState = MutableStateFlow(PartyState())
    val partyState: StateFlow<PartyState> = _partyState.asStateFlow()

    val quizHistory: StateFlow<List<QuizHistoryEntity>> = repository.quizHistory
        .combine(MutableStateFlow(Unit)) { history, _ -> history }
        .let { flow ->
            val state = MutableStateFlow<List<QuizHistoryEntity>>(emptyList())
            viewModelScope.launch {
                repository.quizHistory.collectLatest { state.value = it }
            }
            state.asStateFlow()
        }

    val blitzHighScore: StateFlow<Int> = repository.getHighScore("BLITZ")
        .combine(MutableStateFlow(0)) { score, _ -> score ?: 0 }
        .let { flow ->
            val state = MutableStateFlow(0)
            viewModelScope.launch {
                repository.getHighScore("BLITZ").collectLatest { state.value = it ?: 0 }
            }
            state.asStateFlow()
        }

    private val prefs = application.getSharedPreferences("flagcraft_preferences", android.content.Context.MODE_PRIVATE)

    private val _isSupporter = MutableStateFlow(prefs.getBoolean("is_supporter", false))
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _isSupportDialogVisible = MutableStateFlow(false)
    val isSupportDialogVisible: StateFlow<Boolean> = _isSupportDialogVisible.asStateFlow()

    private val _completedTasksCount = MutableStateFlow(prefs.getInt("completed_tasks", 0))
    val completedTasksCount: StateFlow<Int> = _completedTasksCount.asStateFlow()

    private val _showValueMomentPrompt = MutableStateFlow(false)
    val showValueMomentPrompt: StateFlow<Boolean> = _showValueMomentPrompt.asStateFlow()

    private val _shakeEnabled = MutableStateFlow(prefs.getBoolean("shake_enabled", true))
    val shakeEnabled: StateFlow<Boolean> = _shakeEnabled.asStateFlow()

    private val _shakeThresholdGForce = MutableStateFlow(prefs.getFloat("shake_threshold_g", 2.5f))
    val shakeThresholdGForce: StateFlow<Float> = _shakeThresholdGForce.asStateFlow()

    private var blitzTimerJob: Job? = null

    fun setShakeEnabled(enabled: Boolean) {
        _shakeEnabled.value = enabled
        prefs.edit().putBoolean("shake_enabled", enabled).apply()
    }

    fun setShakeThresholdGForce(threshold: Float) {
        val clamped = threshold.coerceIn(1.5f, 4.5f)
        _shakeThresholdGForce.value = clamped
        prefs.edit().putFloat("shake_threshold_g", clamped).apply()
    }

    fun openSupportDialog() {
        _isSupportDialogVisible.value = true
    }

    fun closeSupportDialog() {
        _isSupportDialogVisible.value = false
    }

    fun dismissValueMomentPrompt() {
        _showValueMomentPrompt.value = false
        prefs.edit().putLong("last_donation_dismiss_time", System.currentTimeMillis()).apply()
    }

    val billingManager = com.example.data.billing.BillingManager(application) { tierId ->
        simulatePlayDonation(tierId)
    }

    fun launchPlayBillingFlow(activity: android.app.Activity, tierId: String) {
        val success = billingManager.launchBillingFlow(activity, tierId)
        if (!success) {
            // Fallback for emulator / non-play-services sandbox environment
            simulatePlayDonation(tierId)
        }
    }

    fun simulatePlayDonation(tierId: String) {
        _isSupporter.value = true
        _showValueMomentPrompt.value = false
        prefs.edit()
            .putBoolean("is_supporter", true)
            .putString("supporter_tier", tierId)
            .apply()
    }

    private fun checkValueMomentTrigger() {
        val count = _completedTasksCount.value + 1
        _completedTasksCount.value = count
        prefs.edit().putInt("completed_tasks", count).apply()

        val isAlreadySupporter = _isSupporter.value
        val lastDismiss = prefs.getLong("last_donation_dismiss_time", 0L)
        val cooldownMillis = 14L * 24 * 60 * 60 * 1000 // 14 days cooldown
        val isCooldownActive = (System.currentTimeMillis() - lastDismiss) < cooldownMillis

        // Satisfaction Safeguard: Only show at verified value realization moment (e.g. 3+ or 7+ completed tasks)
        if (!isAlreadySupporter && !isCooldownActive && (count == 3 || count == 7 || count == 15)) {
            _showValueMomentPrompt.value = true
        }
    }

    init {
        viewModelScope.launch {
            repository.syncCountries()
        }

        viewModelScope.launch {
            repository.allCountries.collectLatest { countries ->
                _allCountries.value = countries
                updateFilteredList()
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        val previousScreen = _currentScreen.value
        _currentScreen.value = screen
        if (previousScreen == AppScreen.BLITZ_QUIZ && screen != AppScreen.BLITZ_QUIZ) {
            blitzTimerJob?.cancel()
            if (_blitzState.value.totalAnswered == 0) {
                _blitzState.value = BlitzState()
            }
        }
        if (screen == AppScreen.BLITZ_QUIZ && (!_blitzState.value.isRunning || _blitzState.value.isGameOver)) {
            startBlitzGame()
        }
    }

    fun setTvPartyMode(enabled: Boolean) {
        _isTvPartyMode.value = enabled
        if (enabled && _currentScreen.value == AppScreen.HOME) {
            navigateTo(AppScreen.PARTY_MODE)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        updateFilteredList()
    }

    fun setSelectedRegion(region: String) {
        _selectedRegion.value = region
        updateFilteredList()
    }

    fun setFavoritesOnly(favoritesOnly: Boolean) {
        _favoritesOnly.value = favoritesOnly
        updateFilteredList()
    }

    private fun updateFilteredList() {
        var list = _allCountries.value
        val query = _searchQuery.value.trim().lowercase()
        val region = _selectedRegion.value
        val fav = _favoritesOnly.value

        if (region != "All") {
            list = list.filter { it.region.equals(region, ignoreCase = true) }
        }
        if (fav) {
            list = list.filter { it.isFavorite }
        }
        if (query.isNotEmpty()) {
            list = list.filter {
                it.name.lowercase().contains(query) ||
                        it.officialName.lowercase().contains(query) ||
                        it.capital.lowercase().contains(query) ||
                        it.code.lowercase().contains(query)
            }
        }
        _filteredCountries.value = list
    }

    fun toggleFavorite(country: Country) {
        viewModelScope.launch {
            repository.toggleFavorite(country.code, country.isFavorite)
        }
    }

    fun updateMasteryLevel(country: Country, newLevel: Int) {
        viewModelScope.launch {
            repository.updateMasteryLevel(country.code, newLevel)
            if (newLevel == 2) {
                val mastered = prefs.getInt("mastered_cards_count", 0) + 1
                prefs.edit().putInt("mastered_cards_count", mastered).apply()
                if (mastered % 10 == 0) {
                    checkValueMomentTrigger()
                }
            }
        }
    }

    // --- 60-Second Blitz Quiz Logic ---
    fun startBlitzGame() {
        blitzTimerJob?.cancel()
        val firstQuestion = generateQuestion() ?: return

        _blitzState.value = BlitzState(
            isRunning = true,
            score = 0,
            streak = 0,
            maxStreak = 0,
            totalAnswered = 0,
            timeLeftSeconds = 60,
            currentQuestion = firstQuestion,
            isGameOver = false
        )

        blitzTimerJob = viewModelScope.launch {
            while (_blitzState.value.timeLeftSeconds > 0) {
                delay(1000)
                val current = _blitzState.value
                if (current.timeLeftSeconds > 1) {
                    _blitzState.value = current.copy(timeLeftSeconds = current.timeLeftSeconds - 1)
                } else {
                    finishBlitzGame()
                    break
                }
            }
        }
    }

    fun answerBlitzQuestion(selectedOptionIndex: Int) {
        val state = _blitzState.value
        if (!state.isRunning || state.isGameOver || state.selectedOptionIndex != null) return
        val question = state.currentQuestion ?: return

        val isCorrect = selectedOptionIndex == question.correctIndex
        val newStreak = if (isCorrect) state.streak + 1 else 0
        val comboMultiplier = (newStreak / 3) + 1
        val points = if (isCorrect) 100 * comboMultiplier else 0
        val newScore = state.score + points

        _blitzState.value = state.copy(
            selectedOptionIndex = selectedOptionIndex,
            isCorrectSelection = isCorrect,
            score = newScore,
            streak = newStreak,
            maxStreak = maxOf(state.maxStreak, newStreak),
            totalAnswered = state.totalAnswered + 1,
            correctAnswers = state.correctAnswers + (if (isCorrect) 1 else 0)
        )

        viewModelScope.launch {
            delay(500)
            val nextQuestion = generateQuestion()
            _blitzState.value = _blitzState.value.copy(
                currentQuestion = nextQuestion,
                selectedOptionIndex = null,
                isCorrectSelection = null
            )
        }
    }

    private fun finishBlitzGame() {
        val finalState = _blitzState.value
        _blitzState.value = finalState.copy(
            isRunning = false,
            isGameOver = true,
            timeLeftSeconds = 0
        )
        checkValueMomentTrigger()
        viewModelScope.launch {
            repository.saveQuizHistory(
                mode = "BLITZ",
                score = finalState.score,
                total = finalState.totalAnswered,
                correctCount = finalState.correctAnswers
            )
        }
    }

    // --- Living Room Android TV Party Mode Logic ---
    fun setPartyPlayerCount(count: Int) {
        val names = (1..count).map { "Player $it" }
        _partyState.value = PartyState(
            playerCount = count,
            playerNames = names,
            playerScores = MutableList(count) { 0 }
        )
    }

    fun startPartyGame() {
        val count = _partyState.value.playerCount
        val names = (1..count).map { "Player $it" }
        val firstQuestion = generateQuestion() ?: return

        _partyState.value = PartyState(
            playerCount = count,
            playerNames = names,
            playerScores = MutableList(count) { 0 },
            currentPlayerTurn = 0,
            currentRound = 1,
            totalRounds = 10 * count,
            currentQuestion = firstQuestion,
            selectedAnswerIndex = null,
            isAnswerRevealed = false,
            isGameOver = false,
            isStarted = true
        )
    }

    fun submitPartyAnswer(directionIndex: Int) { // 0=UP(RED), 1=LEFT(YELLOW), 2=RIGHT(GREEN), 3=DOWN(BLUE)
        val state = _partyState.value
        if (!state.isStarted || state.isGameOver || state.isAnswerRevealed) return
        val question = state.currentQuestion ?: return

        val isCorrect = directionIndex == question.correctIndex
        val updatedScores = state.playerScores.toMutableList()
        if (isCorrect) {
            updatedScores[state.currentPlayerTurn] += 100
        }

        _partyState.value = state.copy(
            selectedAnswerIndex = directionIndex,
            isAnswerRevealed = true,
            playerScores = updatedScores
        )

        viewModelScope.launch {
            delay(1500) // Show answer feedback on big screen
            advancePartyTurn()
        }
    }

    private fun advancePartyTurn() {
        val state = _partyState.value
        val nextTurn = (state.currentPlayerTurn + 1) % state.playerCount
        val nextRound = state.currentRound + 1

        if (nextRound > state.totalRounds) {
            // End of party game
            val maxScore = state.playerScores.maxOrNull() ?: 0
            val winnerIdx = state.playerScores.indexOf(maxScore)
            val winnerName = if (winnerIdx >= 0) state.playerNames[winnerIdx] else "Nobody"

            _partyState.value = state.copy(
                isGameOver = true,
                winnerName = winnerName,
                isAnswerRevealed = false
            )
            checkValueMomentTrigger()
            val questionsPerPlayer = (state.totalRounds / state.playerCount.coerceAtLeast(1)).coerceAtLeast(1)
            val winnerCorrectCount = (maxScore / 100).coerceIn(0, questionsPerPlayer)
            viewModelScope.launch {
                repository.saveQuizHistory(
                    mode = "PARTY",
                    score = maxScore,
                    total = questionsPerPlayer,
                    correctCount = winnerCorrectCount
                )
            }
        } else {
            val nextQuestion = generateQuestion()
            _partyState.value = state.copy(
                currentPlayerTurn = nextTurn,
                currentRound = nextRound,
                currentQuestion = nextQuestion,
                selectedAnswerIndex = null,
                isAnswerRevealed = false
            )
        }
    }

    // --- Helper Question Generator ---
    private fun generateQuestion(): QuizQuestion? {
        val list = _allCountries.value
        if (list.size < 4) return null

        val target = list.random()
        val wrongChoices = list.filter { it.code != target.code }.shuffled().take(3)
        val optionsList = (wrongChoices + target).shuffled()
        val correctIndex = optionsList.indexOf(target)

        val qType = QuestionType.values().random()
        val promptText = when (qType) {
            QuestionType.FLAG_TO_NAME -> "Which country does this flag belong to?"
            QuestionType.NAME_TO_FLAG -> "Select the flag for ${target.name}:"
            QuestionType.CAPITAL_TO_COUNTRY -> "Which country has the capital '${target.capital}'?"
        }

        val options = optionsList.mapIndexed { idx, item ->
            val optionLabel = ('A' + idx)
            val text = when (qType) {
                QuestionType.FLAG_TO_NAME, QuestionType.CAPITAL_TO_COUNTRY -> item.name
                QuestionType.NAME_TO_FLAG -> "Flag $optionLabel"
            }
            val optionFlagPng = when (qType) {
                QuestionType.FLAG_TO_NAME -> ""
                QuestionType.NAME_TO_FLAG, QuestionType.CAPITAL_TO_COUNTRY -> item.flagPng
            }
            QuizOption(
                id = item.code,
                text = text,
                flagPng = optionFlagPng,
                isCorrect = idx == correctIndex
            )
        }

        return QuizQuestion(
            country = target,
            questionType = qType,
            promptText = promptText,
            options = options,
            correctIndex = correctIndex
        )
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
