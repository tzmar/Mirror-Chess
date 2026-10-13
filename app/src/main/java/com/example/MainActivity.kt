package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.EnginesScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.OpeningsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MirrorChessApp()
            }
        }
    }
}

@Composable
fun MirrorChessApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val gameUiState by viewModel.gameUiState.collectAsStateWithLifecycle()

    val gamesCount by viewModel.gamesCount.collectAsStateWithLifecycle()
    val playerWins by viewModel.playerWins.collectAsStateWithLifecycle()
    val botWins by viewModel.botWins.collectAsStateWithLifecycle()
    val draws by viewModel.draws.collectAsStateWithLifecycle()
    val mirrorAccuracy by viewModel.mirrorAccuracy.collectAsStateWithLifecycle()
    val totalMovesCount by viewModel.totalMovesCount.collectAsStateWithLifecycle()
    val openingStats by viewModel.openingStats.collectAsStateWithLifecycle()
    val piecePreferences by viewModel.piecePreferences.collectAsStateWithLifecycle()

    val engineProfiles by viewModel.engineProfiles.collectAsStateWithLifecycle()
    val activeEngineId by viewModel.activeEngineId.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val uciTestOutput by viewModel.uciTestOutput.collectAsStateWithLifecycle()
    val botElo by viewModel.botElo.collectAsStateWithLifecycle()

    // Back handler: return to Game screen if user taps Android Back from sub-screens
    if (currentScreen != AppScreen.GAME) {
        BackHandler {
            viewModel.navigateTo(AppScreen.GAME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.GAME,
                    onClick = { viewModel.navigateTo(AppScreen.GAME) },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Play") },
                    label = { Text("Play", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        indicatorColor = SurfaceElevated,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_play")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.STATS,
                    onClick = { viewModel.navigateTo(AppScreen.STATS) },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Stats") },
                    label = { Text("Stats", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        indicatorColor = SurfaceElevated,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_stats")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.OPENINGS,
                    onClick = { viewModel.navigateTo(AppScreen.OPENINGS) },
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Openings") },
                    label = { Text("Openings", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        indicatorColor = SurfaceElevated,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_openings")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.ENGINES,
                    onClick = { viewModel.navigateTo(AppScreen.ENGINES) },
                    icon = { Icon(Icons.Default.Memory, contentDescription = "Engines") },
                    label = { Text("Engines", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        indicatorColor = SurfaceElevated,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_engines")
                )
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanAccent,
                        selectedTextColor = CyanAccent,
                        indicatorColor = SurfaceElevated,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.GAME -> {
                    GameScreen(
                        state = gameUiState,
                        onSquareClick = { viewModel.onSquareClicked(it) },
                        onPieceDragged = { from, to -> viewModel.onPieceDragged(from, to) },
                        onPromotionChosen = { viewModel.onPromotionChosen(it) },
                        onResignClick = { viewModel.resignGame() },
                        onNewGameClick = { viewModel.startNewGame() }
                    )
                }
                AppScreen.STATS -> {
                    StatsScreen(
                        gamesCount = gamesCount,
                        playerWins = playerWins,
                        botWins = botWins,
                        draws = draws,
                        mirrorAccuracy = mirrorAccuracy,
                        totalMovesRecorded = totalMovesCount,
                        pieceDistribution = piecePreferences
                    )
                }
                AppScreen.OPENINGS -> {
                    OpeningsScreen(
                        openingStats = openingStats
                    )
                }
                AppScreen.ENGINES -> {
                    EnginesScreen(
                        engineProfiles = engineProfiles,
                        activeEngineId = activeEngineId,
                        downloadProgressMap = downloadProgressMap,
                        isConnected = isConnected,
                        uciTestOutput = uciTestOutput,
                        onSelectEngine = { viewModel.selectEngine(it) },
                        onDownloadEngine = { viewModel.downloadEngine(it) },
                        onCancelDownload = { viewModel.cancelDownload(it) },
                        onDeleteEngine = { viewModel.deleteEngine(it) },
                        onImportEngine = { name, uri -> viewModel.importEngine(name, uri) },
                        onTestEngineUci = { viewModel.testEngineUci(it) },
                        onDismissTestDialog = { viewModel.dismissTestDialog() }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentElo = botElo,
                        activeEngineId = activeEngineId,
                        engineProfiles = engineProfiles,
                        onEloChange = { viewModel.setElo(it) },
                        onSelectEngine = { viewModel.selectEngine(it) },
                        onResetStyleData = { viewModel.resetPlayerStyleData() }
                    )
                }
            }
        }
    }
}
