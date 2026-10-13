package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EngineProfileEntity
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentElo: Int,
    activeEngineId: String,
    engineProfiles: List<EngineProfileEntity>,
    onEloChange: (Int) -> Unit,
    onSelectEngine: (String) -> Unit,
    onResetStyleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var showResetDialog by remember { mutableStateOf(false) }
    var engineDropdownExpanded by remember { mutableStateOf(false) }
    var sliderElo by remember(currentElo) { mutableFloatStateOf(currentElo.toFloat()) }

    val activeEngine = engineProfiles.firstOrNull { it.id == activeEngineId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = CyanAccent,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Engine Selector Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("engine_selection_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = CyanAccent)
                    Text(
                        text = "Active Chess Engine",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "If selected engine fails to load, the app automatically falls back to Stockfish Embedded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = engineDropdownExpanded,
                    onExpandedChange = { engineDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = activeEngine?.name ?: "Stockfish Embedded (UCI)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = engineDropdownExpanded,
                        onDismissRequest = { engineDropdownExpanded = false }
                    ) {
                        engineProfiles.filter { it.isBuiltIn || it.isDownloaded }.forEach { profile ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(profile.name, fontWeight = FontWeight.Bold)
                                        Text("v${profile.version}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    onSelectEngine(profile.id)
                                    engineDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Elo Strength Slider Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("elo_strength_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = AmberAccent)
                        Text(
                            text = "Engine Strength (UCI_Elo)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "${sliderElo.toInt()} Elo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberAccent
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        sliderElo < 800 -> "Novice: Blunder prone, tactical oversights."
                        sliderElo < 1500 -> "Club Player: Solid fundamentals, occasional tactics."
                        sliderElo < 2000 -> "Expert: Positional awareness, deep candidate moves."
                        else -> "Master / Grandmaster: Deep minimax alpha-beta tactical search."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = sliderElo,
                    onValueChange = { sliderElo = it },
                    onValueChangeFinished = { onEloChange(sliderElo.toInt()) },
                    valueRange = 400f..2600f,
                    steps = 21,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberAccent,
                        activeTrackColor = AmberAccent,
                        inactiveTrackColor = SurfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("elo_slider")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("400 (Novice)", fontSize = 11.sp, color = TextSecondary)
                    Text("1500 (Club)", fontSize = 11.sp, color = TextSecondary)
                    Text("2600 (Master)", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        // Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CyanAccent)
                        Column {
                            Text(
                                text = "Move Sound & Haptics",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Audio cues when making legal moves",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = SurfaceHighlight)
                    )
                }
            }
        }

        // Reset Player Style Data Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("reset_style_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444))
                    Text(
                        text = "Reset Player Style Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
                Text(
                    text = "Erases your recorded move patterns, opening repertoire, and game history from the local database. The bot will return to baseline novice Game 1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth().testTag("reset_style_button")
                ) {
                    Text("Clear All Style Data & Reset to Game 1", fontWeight = FontWeight.Bold)
                }
            }
        }

        // About / Offline Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceHighlight)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = CyanAccent)
                Column {
                    Text("100% Offline Architecture", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        "All games, engine computations, and neural memory run strictly on-device without internet access. No account or cloud sync required.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Confirmation Dialog for Resetting Data
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Player Style Memory?") },
            text = {
                Text("This permanently deletes all your recorded moves and game stats. The bot will start over as a baseline Game 1 opponent.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetStyleData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Yes, Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
