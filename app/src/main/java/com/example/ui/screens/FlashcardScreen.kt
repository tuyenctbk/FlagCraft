package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.common.shake.UniversalShakeDetector
import com.common.shake.ui.ShakeSensitivitySettingCard
import com.example.R
import com.example.ui.components.SwipeableFlashcard
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun FlashcardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    val countries by viewModel.filteredCountries.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val favoritesOnly by viewModel.favoritesOnly.collectAsState()
    val shakeEnabled by viewModel.shakeEnabled.collectAsState()
    val shakeThreshold by viewModel.shakeThresholdGForce.collectAsState()

    var currentIndex by remember(countries.size, selectedRegion, favoritesOnly) { mutableIntStateOf(0) }
    var showShakeSettings by remember { mutableStateOf(false) }

    val context = LocalContext.current
    DisposableEffect(shakeEnabled, shakeThreshold, countries.size) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val detector = UniversalShakeDetector {
            if (countries.isNotEmpty()) {
                currentIndex = countries.indices.random()
            }
        }.apply {
            isEnabled = shakeEnabled
            thresholdGForce = shakeThreshold
        }
        if (sensorManager != null && accelerometer != null && shakeEnabled) {
            sensorManager.registerListener(detector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager?.unregisterListener(detector)
        }
    }

    val regions = listOf("All", "Africa", "Americas", "Asia", "Europe", "Oceania")

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("flashcard_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.btn_back))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.flashcards_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showShakeSettings = !showShakeSettings },
                        modifier = Modifier.testTag("shake_settings_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = stringResource(R.string.shake_setting_title),
                            tint = if (showShakeSettings || shakeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setFavoritesOnly(!favoritesOnly) },
                        modifier = Modifier.testTag("filter_favorites_toggle")
                    ) {
                        Icon(
                            imageVector = if (favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.filter_favorites_only),
                            tint = if (favoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showShakeSettings) {
                ShakeSensitivitySettingCard(
                    isEnabled = shakeEnabled,
                    thresholdGForce = shakeThreshold,
                    onEnabledChanged = { viewModel.setShakeEnabled(it) },
                    onThresholdChanged = { viewModel.setShakeThresholdGForce(it) },
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            // Region Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                regions.forEach { region ->
                    val regionLabel = when (region) {
                        "Africa" -> stringResource(R.string.region_africa)
                        "Americas" -> stringResource(R.string.region_americas)
                        "Asia" -> stringResource(R.string.region_asia)
                        "Europe" -> stringResource(R.string.region_europe)
                        "Oceania" -> stringResource(R.string.region_oceania)
                        else -> stringResource(R.string.region_all)
                    }
                    FilterChip(
                        selected = selectedRegion == region,
                        onClick = {
                            viewModel.setSelectedRegion(region)
                            currentIndex = 0
                        },
                        label = { Text(regionLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (countries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.flashcards_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.flashcards_empty_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            } else {
                val safeIndex = currentIndex.coerceIn(0, countries.size - 1)
                val currentCountry = countries[safeIndex]
                val progress = (safeIndex + 1).toFloat() / countries.size

                // Deck Progress
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.flashcards_card_counter, safeIndex + 1, countries.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (currentCountry.masteryLevel == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (currentCountry.masteryLevel == 2) stringResource(R.string.flashcards_status_mastered) else stringResource(R.string.flashcards_status_learning),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Swipeable / Flippable Flashcard
                SwipeableFlashcard(
                    country = currentCountry,
                    onToggleFavorite = { viewModel.toggleFavorite(currentCountry) },
                    onMarkMastered = {
                        viewModel.updateMasteryLevel(currentCountry, 2)
                        if (currentIndex < countries.size - 1) currentIndex++ else currentIndex = 0
                    },
                    onMarkNeedPractice = {
                        viewModel.updateMasteryLevel(currentCountry, 1)
                        if (currentIndex < countries.size - 1) currentIndex++ else currentIndex = 0
                    },
                    modifier = Modifier.weight(1f)
                )

                // Value Realization Milestone Card (e.g. Mastered 10 cards)
                val showValueMoment by viewModel.showValueMomentPrompt.collectAsState()
                if (showValueMoment) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flashcard_value_moment_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎯 Learning Milestone!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "FlagCraft is 100% ad-free & indie. Enjoying the flashcards? Support development!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Later",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { viewModel.dismissValueMomentPrompt() }
                                        .padding(6.dp)
                                        .testTag("flashcard_moment_dismiss")
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = { viewModel.openSupportDialog() },
                                    modifier = Modifier.testTag("flashcard_support_button")
                                ) {
                                    Text("☕", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedIconButton(
                        onClick = {
                            if (currentIndex > 0) currentIndex-- else currentIndex = countries.size - 1
                        },
                        modifier = Modifier.testTag("prev_card_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Card")
                    }

                    OutlinedIconButton(
                        onClick = {
                            if (countries.isNotEmpty()) currentIndex = countries.indices.random()
                        },
                        modifier = Modifier.testTag("shuffle_card_button")
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle Card")
                    }

                    OutlinedIconButton(
                        onClick = {
                            if (currentIndex < countries.size - 1) currentIndex++ else currentIndex = 0
                        },
                        modifier = Modifier.testTag("next_card_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Card")
                    }
                }
            }
        }
    }
}
