package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.ui.components.SupportDialog
import com.example.ui.screens.BlitzQuizScreen
import com.example.ui.screens.CountryExplorerScreen
import com.example.ui.screens.FlashcardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PartyModeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.FlagCraftTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlagCraftTheme {
                var showSplash by remember { mutableStateOf(true) }

                Box(modifier = Modifier.fillMaxSize()) {
                    FlagCraftApp()

                    AnimatedVisibility(
                        visible = showSplash,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlagCraftApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isSupporter by viewModel.isSupporter.collectAsState()
    val isSupportDialogVisible by viewModel.isSupportDialogVisible.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    if (isSupportDialogVisible) {
        SupportDialog(
            isSupporter = isSupporter,
            onDismiss = { viewModel.closeSupportDialog() },
            onSimulatePlayPurchase = { tierId ->
                if (activity != null) {
                    viewModel.launchPlayBillingFlow(activity, tierId)
                } else {
                    viewModel.simulatePlayDonation(tierId)
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp
        val isImmersiveGame = currentScreen == AppScreen.PARTY_MODE || currentScreen == AppScreen.BLITZ_QUIZ

        if (isWideScreen && !isImmersiveGame) {
            // Tablet / Desktop / Android TV layout with NavigationRail on the left
            Row(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag("side_navigation_rail"),
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.openSupportDialog() },
                                modifier = Modifier.testTag("rail_support_button")
                            ) {
                                Icon(
                                    imageVector = if (isSupporter) Icons.Default.Star else Icons.Default.Favorite,
                                    contentDescription = "Support Development",
                                    tint = if (isSupporter) Color(0xFFF59E0B) else Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                ) {
                    NavigationRailItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) },
                        modifier = Modifier.testTag("rail_nav_home")
                    )

                    NavigationRailItem(
                        selected = currentScreen == AppScreen.FLASHCARD,
                        onClick = { viewModel.navigateTo(AppScreen.FLASHCARD) },
                        icon = { Icon(imageVector = Icons.Default.School, contentDescription = stringResource(R.string.nav_cards)) },
                        label = { Text(stringResource(R.string.nav_cards)) },
                        modifier = Modifier.testTag("rail_nav_cards")
                    )

                    NavigationRailItem(
                        selected = currentScreen == AppScreen.BLITZ_QUIZ,
                        onClick = { viewModel.navigateTo(AppScreen.BLITZ_QUIZ) },
                        icon = { Icon(imageVector = Icons.Default.Bolt, contentDescription = stringResource(R.string.nav_blitz)) },
                        label = { Text(stringResource(R.string.nav_blitz)) },
                        modifier = Modifier.testTag("rail_nav_blitz")
                    )

                    NavigationRailItem(
                        selected = currentScreen == AppScreen.PARTY_MODE,
                        onClick = { viewModel.navigateTo(AppScreen.PARTY_MODE) },
                        icon = { Icon(imageVector = Icons.Default.Tv, contentDescription = stringResource(R.string.nav_party)) },
                        label = { Text(stringResource(R.string.nav_party)) },
                        modifier = Modifier.testTag("rail_nav_party")
                    )

                    NavigationRailItem(
                        selected = currentScreen == AppScreen.EXPLORER,
                        onClick = { viewModel.navigateTo(AppScreen.EXPLORER) },
                        icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = stringResource(R.string.nav_explorer)) },
                        label = { Text(stringResource(R.string.nav_explorer)) },
                        modifier = Modifier.testTag("rail_nav_explorer")
                    )

                    NavigationRailItem(
                        selected = currentScreen == AppScreen.STATS,
                        onClick = { viewModel.navigateTo(AppScreen.STATS) },
                        icon = { Icon(imageVector = Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_stats)) },
                        label = { Text(stringResource(R.string.nav_stats)) },
                        modifier = Modifier.testTag("rail_nav_stats")
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(currentScreen = currentScreen, viewModel = viewModel, modifier = Modifier.fillMaxSize())
                }
            }
        } else {
            // Standard Mobile / Handheld layout with bottom NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (!isImmersiveGame) {
                        NavigationBar(modifier = Modifier.testTag("bottom_navigation_bar")) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.HOME,
                                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = stringResource(R.string.nav_home)) },
                                label = { Text(stringResource(R.string.nav_home)) },
                                modifier = Modifier.testTag("nav_home")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.FLASHCARD,
                                onClick = { viewModel.navigateTo(AppScreen.FLASHCARD) },
                                icon = { Icon(imageVector = Icons.Default.School, contentDescription = stringResource(R.string.nav_cards)) },
                                label = { Text(stringResource(R.string.nav_cards)) },
                                modifier = Modifier.testTag("nav_cards")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.BLITZ_QUIZ,
                                onClick = { viewModel.navigateTo(AppScreen.BLITZ_QUIZ) },
                                icon = { Icon(imageVector = Icons.Default.Bolt, contentDescription = stringResource(R.string.nav_blitz)) },
                                label = { Text(stringResource(R.string.nav_blitz)) },
                                modifier = Modifier.testTag("nav_blitz")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.PARTY_MODE,
                                onClick = { viewModel.navigateTo(AppScreen.PARTY_MODE) },
                                icon = { Icon(imageVector = Icons.Default.Tv, contentDescription = stringResource(R.string.nav_party)) },
                                label = { Text(stringResource(R.string.nav_party)) },
                                modifier = Modifier.testTag("nav_party")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.EXPLORER,
                                onClick = { viewModel.navigateTo(AppScreen.EXPLORER) },
                                icon = { Icon(imageVector = Icons.Default.Explore, contentDescription = stringResource(R.string.nav_explorer)) },
                                label = { Text(stringResource(R.string.nav_explorer)) },
                                modifier = Modifier.testTag("nav_explorer")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                ScreenContent(
                    currentScreen = currentScreen,
                    viewModel = viewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ScreenContent(
    currentScreen: AppScreen,
    viewModel: MainViewModel,
    modifier: Modifier
) {
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "screen_transition"
    ) { targetScreen ->
        when (targetScreen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.FLASHCARD -> FlashcardScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.BLITZ_QUIZ -> BlitzQuizScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.PARTY_MODE -> PartyModeScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.EXPLORER -> CountryExplorerScreen(viewModel = viewModel, modifier = modifier)
            AppScreen.STATS -> StatsScreen(viewModel = viewModel, modifier = modifier)
        }
    }
}
