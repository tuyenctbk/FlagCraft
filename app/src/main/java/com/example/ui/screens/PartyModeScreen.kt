package com.example.ui.screens

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.components.DPadAnswerButton
import com.example.ui.components.DPadDirection
import com.example.ui.components.FlagImage
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuestionType

@Composable
fun PartyModeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val partyState by viewModel.partyState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(partyState.isStarted, partyState.currentRound, partyState.isAnswerRevealed) {
        if (partyState.isStarted && !partyState.isGameOver && !partyState.isAnswerRevealed) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onKeyEvent { keyEvent ->
                val isActiveRound = partyState.isStarted && !partyState.isGameOver && !partyState.isAnswerRevealed
                if (isActiveRound && keyEvent.type == androidx.compose.ui.input.key.KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_1 -> {
                            viewModel.submitPartyAnswer(0) // UP = RED
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_2 -> {
                            viewModel.submitPartyAnswer(1) // LEFT = YELLOW
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_3 -> {
                            viewModel.submitPartyAnswer(2) // RIGHT = GREEN
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_4 -> {
                            viewModel.submitPartyAnswer(3) // DOWN = BLUE
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusRequester(focusRequester)
            .then(if (partyState.isStarted && !partyState.isGameOver) Modifier.focusable() else Modifier)
    ) {
        if (!partyState.isStarted) {
            // Setup Player Count Screen
            PartySetupView(
                partyState = partyState,
                onSelectPlayerCount = { viewModel.setPartyPlayerCount(it) },
                onStartGame = { viewModel.startPartyGame() },
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        } else if (partyState.isGameOver) {
            val showValueMoment by viewModel.showValueMomentPrompt.collectAsState()
            // Living Room Showdown Winner Screen
            PartyWinnerView(
                partyState = partyState,
                showValueMoment = showValueMoment,
                onSupportClick = { viewModel.openSupportDialog() },
                onDismissValueMoment = { viewModel.dismissValueMomentPrompt() },
                onPlayAgain = { viewModel.startPartyGame() },
                onHome = { viewModel.navigateTo(AppScreen.HOME) }
            )
        } else {
            // Main TV Trivia Round View
            PartyGameRoundView(
                partyState = partyState,
                onAnswerSelected = { viewModel.submitPartyAnswer(it) },
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }
    }
}

@Composable
private fun PartySetupView(
    partyState: com.example.ui.viewmodel.PartyState,
    onSelectPlayerCount: (Int) -> Unit,
    onStartGame: () -> Unit,
    onBack: () -> Unit
) {
    val setupStartButtonFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        setupStartButtonFocusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("party_setup_back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.party_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_setup_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.party_subtitle),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = stringResource(R.string.party_how_to_play_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.party_select_players),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        listOf(1, 2, 3, 4).forEachIndexed { index, count ->
                            SegmentedButton(
                                selected = partyState.playerCount == count,
                                onClick = { onSelectPlayerCount(count) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                                icon = {},
                                modifier = Modifier.testTag("player_count_$count")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = if (count == 1) "Player" else "Players",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = onStartGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .focusRequester(setupStartButtonFocusRequester)
                            .testTag("start_party_game_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.party_btn_start),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PartyGameRoundView(
    partyState: com.example.ui.viewmodel.PartyState,
    onAnswerSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val question = partyState.currentQuestion ?: return
    val currentPlayerName = partyState.playerNames[partyState.currentPlayerTurn]

    val playerColors = listOf(
        Color(0xFF3B82F6), // P1 Blue
        Color(0xFFEF4444), // P2 Red
        Color(0xFF10B981), // P3 Green
        Color(0xFFF59E0B)  // P4 Amber
    )
    val activeColor = playerColors[partyState.currentPlayerTurn % playerColors.size]

    val directions = listOf(
        DPadDirection.UP,
        DPadDirection.LEFT,
        DPadDirection.RIGHT,
        DPadDirection.DOWN
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = maxWidth >= 680.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("party_round_back_button")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                }

                // Current Turn Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(activeColor)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "👉 " + stringResource(R.string.party_current_turn, currentPlayerName),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = stringResource(R.string.party_round_counter, partyState.currentRound, partyState.totalRounds),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Live Scores Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                itemsIndexed(partyState.playerNames) { idx, name ->
                    val pColor = playerColors[idx % playerColors.size]
                    val isTurn = idx == partyState.currentPlayerTurn

                    Card(
                        modifier = Modifier.padding(horizontal = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTurn) pColor else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "$name: ${partyState.playerScores[idx]} pts",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isTurn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Main Question Banner & Flag
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = question.promptText,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isWide) 24.sp else 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(14.dp))

                when (question.questionType) {
                    QuestionType.FLAG_TO_NAME -> {
                        FlagImage(
                            flagUrl = question.country.flagPng,
                            contentDescription = "Target Flag",
                            modifier = Modifier
                                .widthIn(max = if (isWide) 320.dp else 240.dp)
                                .height(if (isWide) 180.dp else 140.dp)
                        )
                    }
                    QuestionType.NAME_TO_FLAG -> {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = question.country.name,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${question.country.region} • ${question.country.subregion}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    QuestionType.CAPITAL_TO_COUNTRY -> {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = question.country.capital,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Continent: ${question.country.region}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // D-Pad Directional Answer Buttons (2x2 on wide/TV, vertical on narrow phone)
            if (isWide) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // UP (RED)
                        DPadAnswerButton(
                            direction = directions[0],
                            optionText = question.options[0].text,
                            flagPngUrl = question.options[0].flagPng,
                            isSelected = partyState.selectedAnswerIndex == 0,
                            isCorrect = if (partyState.isAnswerRevealed) 0 == question.correctIndex else null,
                            isRevealed = partyState.isAnswerRevealed,
                            onClick = { onAnswerSelected(0) },
                            modifier = Modifier.weight(1f)
                        )
                        // LEFT (YELLOW)
                        DPadAnswerButton(
                            direction = directions[1],
                            optionText = question.options[1].text,
                            flagPngUrl = question.options[1].flagPng,
                            isSelected = partyState.selectedAnswerIndex == 1,
                            isCorrect = if (partyState.isAnswerRevealed) 1 == question.correctIndex else null,
                            isRevealed = partyState.isAnswerRevealed,
                            onClick = { onAnswerSelected(1) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // RIGHT (GREEN)
                        DPadAnswerButton(
                            direction = directions[2],
                            optionText = question.options[2].text,
                            flagPngUrl = question.options[2].flagPng,
                            isSelected = partyState.selectedAnswerIndex == 2,
                            isCorrect = if (partyState.isAnswerRevealed) 2 == question.correctIndex else null,
                            isRevealed = partyState.isAnswerRevealed,
                            onClick = { onAnswerSelected(2) },
                            modifier = Modifier.weight(1f)
                        )
                        // DOWN (BLUE)
                        DPadAnswerButton(
                            direction = directions[3],
                            optionText = question.options[3].text,
                            flagPngUrl = question.options[3].flagPng,
                            isSelected = partyState.selectedAnswerIndex == 3,
                            isCorrect = if (partyState.isAnswerRevealed) 3 == question.correctIndex else null,
                            isRevealed = partyState.isAnswerRevealed,
                            onClick = { onAnswerSelected(3) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    directions.forEachIndexed { index, dPadDir ->
                        val option = question.options[index]
                        val isSelected = partyState.selectedAnswerIndex == index
                        val isCorrect = index == question.correctIndex

                        DPadAnswerButton(
                            direction = dPadDir,
                            optionText = option.text,
                            flagPngUrl = option.flagPng,
                            isSelected = isSelected,
                            isCorrect = if (partyState.isAnswerRevealed) isCorrect else null,
                            isRevealed = partyState.isAnswerRevealed,
                            onClick = { onAnswerSelected(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartyWinnerView(
    partyState: com.example.ui.viewmodel.PartyState,
    showValueMoment: Boolean,
    onSupportClick: () -> Unit,
    onDismissValueMoment: () -> Unit,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val setupStartButtonFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        setupStartButtonFocusRequester.requestFocus()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("party_winner_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🏆 " + stringResource(R.string.party_winner_subtitle, partyState.winnerName) + " 🏆",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF59E0B)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(R.string.party_winner_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Final Scoreboard
                    Text(
                        text = stringResource(R.string.party_scores_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    partyState.playerNames.forEachIndexed { idx, name ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${partyState.playerScores[idx]} pts",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Value Realization Moment (e.g. Completed Sofa Trivia Showdown)
                    if (showValueMoment) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("party_value_moment_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.blitz_value_moment_title),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.blitz_value_moment_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.blitz_value_moment_later),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .clickable { onDismissValueMoment() }
                                            .padding(8.dp)
                                            .testTag("party_value_moment_dismiss")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = onSupportClick,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.testTag("party_value_moment_support")
                                    ) {
                                        Text(stringResource(R.string.blitz_value_moment_support), style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onHome,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.btn_home))
                        }

                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(setupStartButtonFocusRequester)
                                .testTag("party_replay_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.btn_play_again))
                        }
                    }
                }
            }
        }
    }
}
