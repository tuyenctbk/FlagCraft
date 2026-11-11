package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isTvPartyMode by viewModel.isTvPartyMode.collectAsState()
    val blitzHighScore by viewModel.blitzHighScore.collectAsState()
    val allCountries by viewModel.allCountries.collectAsState()
    val isSupporter by viewModel.isSupporter.collectAsState()

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = maxWidth >= 680.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 980.dp)
                .padding(horizontal = if (isWide) 24.dp else 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Hero Header Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_hero_banner"),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E3A8A),
                                        Color(0xFF3B82F6),
                                        Color(0xFF06B6D4)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = "FlagCraft",
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "FlagCraft",
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White
                                                )
                                            )
                                            if (isSupporter) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFF59E0B))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.home_supporter_badge),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            fontSize = 9.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = stringResource(R.string.home_tagline),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.openSupportDialog() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("support_heart_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                                            contentDescription = stringResource(R.string.home_support_development),
                                            tint = if (isSupporter) Color(0xFFF59E0B) else Color(0xFFFF6B6B),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // TV Mode Quick Toggle
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = stringResource(R.string.home_tv_mode),
                                        tint = if (isTvPartyMode) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Switch(
                                        checked = isTvPartyMode,
                                        onCheckedChange = { viewModel.setTvPartyMode(it) },
                                        modifier = Modifier.testTag("tv_mode_switch"),
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFFF59E0B),
                                            checkedTrackColor = Color.White.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = if (isTvPartyMode) stringResource(R.string.home_tv_mode_active_title) else stringResource(R.string.home_ready_title),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = if (isTvPartyMode) stringResource(R.string.home_tv_mode_active_desc)
                                else stringResource(R.string.home_ready_desc, allCountries.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            val masteredCount = allCountries.count { it.masteryLevel == 2 }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HeroStatChip(
                                    icon = Icons.Default.Public,
                                    label = "${allCountries.size} Nations",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.weight(1f)
                                )
                                HeroStatChip(
                                    icon = Icons.Default.CheckCircle,
                                    label = "$masteredCount Mastered",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.weight(1f)
                                )
                                HeroStatChip(
                                    icon = Icons.Default.EmojiEvents,
                                    label = "Best: $blitzHighScore",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Mode Selection Header
            item {
                Text(
                    text = stringResource(R.string.home_select_experience),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Game Modes List
            if (isWide) {
                // 2-Column Responsive Layout for Tablets/TV
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameModeCard(
                            title = stringResource(R.string.mode_blitz_title),
                            subtitle = stringResource(R.string.mode_blitz_subtitle, blitzHighScore),
                            icon = Icons.Default.Bolt,
                            gradientColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B)),
                            testTag = "blitz_mode_card",
                            onClick = { viewModel.navigateTo(AppScreen.BLITZ_QUIZ) },
                            modifier = Modifier.weight(1f)
                        )

                        GameModeCard(
                            title = stringResource(R.string.mode_party_title),
                            subtitle = stringResource(R.string.mode_party_subtitle),
                            icon = Icons.Default.Tv,
                            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),
                            testTag = "party_mode_card",
                            onClick = { viewModel.navigateTo(AppScreen.PARTY_MODE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameModeCard(
                            title = stringResource(R.string.mode_flashcards_title),
                            subtitle = stringResource(R.string.mode_flashcards_subtitle),
                            icon = Icons.Default.School,
                            gradientColors = listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
                            testTag = "flashcards_mode_card",
                            onClick = { viewModel.navigateTo(AppScreen.FLASHCARD) },
                            modifier = Modifier.weight(1f)
                        )

                        GameModeCard(
                            title = stringResource(R.string.mode_explorer_title),
                            subtitle = stringResource(R.string.mode_explorer_subtitle),
                            icon = Icons.Default.Explore,
                            gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                            testTag = "explorer_mode_card",
                            onClick = { viewModel.navigateTo(AppScreen.EXPLORER) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    GameModeCard(
                        title = stringResource(R.string.mode_stats_title),
                        subtitle = stringResource(R.string.mode_stats_subtitle),
                        icon = Icons.Default.BarChart,
                        gradientColors = listOf(Color(0xFF64748B), Color(0xFF334155)),
                        testTag = "stats_mode_card",
                        onClick = { viewModel.navigateTo(AppScreen.STATS) }
                    )
                }
            } else {
                // Single column for phones
                item {
                    GameModeCard(
                        title = stringResource(R.string.mode_blitz_title),
                        subtitle = stringResource(R.string.mode_blitz_subtitle_phone, blitzHighScore),
                        icon = Icons.Default.Bolt,
                        gradientColors = listOf(Color(0xFFEF4444), Color(0xFFF59E0B)),
                        testTag = "blitz_mode_card",
                        onClick = { viewModel.navigateTo(AppScreen.BLITZ_QUIZ) }
                    )
                }

                item {
                    GameModeCard(
                        title = stringResource(R.string.mode_party_title),
                        subtitle = stringResource(R.string.mode_party_subtitle_phone),
                        icon = Icons.Default.Tv,
                        gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)),
                        testTag = "party_mode_card",
                        onClick = { viewModel.navigateTo(AppScreen.PARTY_MODE) }
                    )
                }

                item {
                    GameModeCard(
                        title = stringResource(R.string.mode_flashcards_title),
                        subtitle = stringResource(R.string.mode_flashcards_subtitle_phone),
                        icon = Icons.Default.School,
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF06B6D4)),
                        testTag = "flashcards_mode_card",
                        onClick = { viewModel.navigateTo(AppScreen.FLASHCARD) }
                    )
                }

                item {
                    GameModeCard(
                        title = stringResource(R.string.mode_explorer_title),
                        subtitle = stringResource(R.string.mode_explorer_subtitle),
                        icon = Icons.Default.Explore,
                        gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)),
                        testTag = "explorer_mode_card",
                        onClick = { viewModel.navigateTo(AppScreen.EXPLORER) }
                    )
                }

                item {
                    GameModeCard(
                        title = stringResource(R.string.mode_stats_title),
                        subtitle = stringResource(R.string.mode_stats_subtitle),
                        icon = Icons.Default.BarChart,
                        gradientColors = listOf(Color(0xFF64748B), Color(0xFF334155)),
                        testTag = "stats_mode_card",
                        onClick = { viewModel.navigateTo(AppScreen.STATS) }
                    )
                }
            }

            // Static, Non-Intrusive Support Development Section (Permanent access in Settings/Home)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_development_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.support_card_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = stringResource(R.string.support_card_tagline),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isSupporter) stringResource(R.string.support_card_thank_you)
                            else stringResource(R.string.support_card_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.openSupportDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSupporter) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSupporter) stringResource(R.string.support_card_btn_active) else stringResource(R.string.support_card_btn_donate),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradientColors: List<Color>,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.03f else 1f,
        animationSpec = spring(),
        label = "mode_card_focus_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isFocused) Modifier.border(3.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(20.dp))
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 10.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
        }
    }
}
