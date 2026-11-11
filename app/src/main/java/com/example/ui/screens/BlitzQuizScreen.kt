package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.components.FlagImage
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.QuestionType

@Composable
fun BlitzQuizScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val state by viewModel.blitzState.collectAsState()

    // Pulse animation when time is critical (<= 10 seconds)
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val timerPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state.timeLeftSeconds in 1..10) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = maxWidth >= 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 700.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar + Timer Progress Bar grouped together at top
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("blitz_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }

                    // Timer Badge with Pulse
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = timerPulseScale
                                scaleY = timerPulseScale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (state.timeLeftSeconds <= 10) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.blitz_timer),
                            tint = if (state.timeLeftSeconds <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.timeLeftSeconds}s",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (state.timeLeftSeconds <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Score & Combo
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.streak >= 3) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${(state.streak / 3) + 1}x Combo",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        val animatedScore by animateIntAsState(
                            targetValue = state.score,
                            animationSpec = tween(durationMillis = 300),
                            label = "score_anim"
                        )
                        Text(
                            text = stringResource(R.string.blitz_score_label, animatedScore),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Timer Progress Bar directly under Top Bar
                LinearProgressIndicator(
                    progress = { state.timeLeftSeconds / 60f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = if (state.timeLeftSeconds <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (state.isGameOver) {
                // GameOver Score Card centered in remaining screen space
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.animation.AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .testTag("blitz_game_over_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = stringResource(R.string.blitz_times_up),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = stringResource(R.string.blitz_final_score, state.score),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatBox(title = stringResource(R.string.blitz_stat_answered), value = "${state.totalAnswered}")
                                StatBox(title = stringResource(R.string.blitz_stat_max_streak), value = "${state.maxStreak}")
                            }

                            // Value Realization Moment (e.g. 3rd, 7th quiz completed)
                            val showValueMoment by viewModel.showValueMomentPrompt.collectAsState()
                            if (showValueMoment) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("blitz_value_moment_card"),
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
                                                    .clickable { viewModel.dismissValueMomentPrompt() }
                                                    .padding(8.dp)
                                                    .testTag("blitz_value_moment_dismiss")
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Button(
                                                onClick = { viewModel.openSupportDialog() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                ),
                                                modifier = Modifier.testTag("blitz_value_moment_support")
                                            ) {
                                                Text(stringResource(R.string.blitz_value_moment_support), style = MaterialTheme.typography.labelLarge)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.btn_home))
                                }

                                Button(
                                    onClick = { viewModel.startBlitzGame() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("play_again_button"),
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
            } else {
                val question = state.currentQuestion
                if (question != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = question.promptText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                         // Question Visual Prompt
                        when (question.questionType) {
                            QuestionType.FLAG_TO_NAME -> {
                                FlagImage(
                                    flagUrl = question.country.flagPng,
                                    contentDescription = "Target Flag",
                                    modifier = Modifier
                                        .width(220.dp)
                                        .height(130.dp)
                                )
                            }
                            QuestionType.NAME_TO_FLAG -> {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = question.country.name,
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${question.country.region} • ${question.country.subregion}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                            QuestionType.CAPITAL_TO_COUNTRY -> {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationCity,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = question.country.capital,
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = "Region: ${question.country.region}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 4 Options Grid (2x2 on wide screen, vertical list on mobile)
                        AnimatedContent(
                            targetState = question,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
                            },
                            label = "blitz_question_options_transition"
                        ) { currentQ ->
                            if (isWide) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        QuizOptionCard(
                                            question = currentQ,
                                            index = 0,
                                            state = state,
                                            onClick = { viewModel.answerBlitzQuestion(0) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuizOptionCard(
                                            question = currentQ,
                                            index = 1,
                                            state = state,
                                            onClick = { viewModel.answerBlitzQuestion(1) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        QuizOptionCard(
                                            question = currentQ,
                                            index = 2,
                                            state = state,
                                            onClick = { viewModel.answerBlitzQuestion(2) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        QuizOptionCard(
                                            question = currentQ,
                                            index = 3,
                                            state = state,
                                            onClick = { viewModel.answerBlitzQuestion(3) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    currentQ.options.forEachIndexed { index, _ ->
                                        QuizOptionCard(
                                            question = currentQ,
                                            index = index,
                                            state = state,
                                            onClick = { viewModel.answerBlitzQuestion(index) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizOptionCard(
    question: com.example.ui.viewmodel.QuizQuestion,
    index: Int,
    state: com.example.ui.viewmodel.BlitzState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val option = question.options[index]
    val isSelected = state.selectedOptionIndex == index
    val isCorrect = index == question.correctIndex
    val isAnswered = state.selectedOptionIndex != null

    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1f,
        animationSpec = spring(),
        label = "blitz_option_scale"
    )

    val containerColor = when {
        isAnswered && isCorrect -> Color(0xFF10B981)
        isAnswered && isSelected && !isCorrect -> Color(0xFFEF4444)
        isFocused -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .testTag("blitz_option_$index")
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isFocused) Modifier.border(2.5.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(16.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(enabled = !isAnswered, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 8.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (option.flagPng.isNotEmpty()) {
                FlagImage(
                    flagUrl = option.flagPng,
                    contentDescription = option.text,
                    modifier = if (question.questionType == QuestionType.NAME_TO_FLAG) {
                        Modifier.size(width = 72.dp, height = 46.dp)
                    } else {
                        Modifier.size(width = 48.dp, height = 32.dp)
                    }
                )
                Spacer(modifier = Modifier.width(14.dp))
            }

            Text(
                text = option.text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatBox(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
