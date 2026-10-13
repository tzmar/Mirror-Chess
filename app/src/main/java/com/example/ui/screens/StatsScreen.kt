package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PieceCount
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatsScreen(
    gamesCount: Int,
    playerWins: Int,
    botWins: Int,
    draws: Int,
    mirrorAccuracy: Int,
    totalMovesRecorded: Int,
    pieceDistribution: List<PieceCount>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Mirror Performance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        // Hero Mirror Accuracy Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("mirror_accuracy_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanAccent, AmberAccent)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Mirror Accuracy",
                        tint = CyanAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Mirror Accuracy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = SurfaceHighlight,
                        strokeWidth = 10.dp
                    )
                    CircularProgressIndicator(
                        progress = { (mirrorAccuracy.coerceIn(0, 100)) / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = CyanAccent,
                        strokeWidth = 10.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$mirrorAccuracy%",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Style Match",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (gamesCount <= 1)
                        "Play more games to train the bot on your opening prep, piece biases, and tactical habits."
                    else
                        "The bot has matched $mirrorAccuracy% of its moves directly with your historical responses.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Match Record Card: Wins / Losses / Draws
        Card(
            modifier = Modifier.fillMaxWidth().testTag("match_record_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Match Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatMetricItem(label = "Games", value = gamesCount.toString(), color = TextPrimary)
                    StatMetricItem(label = "Wins", value = playerWins.toString(), color = EmeraldAccent)
                    StatMetricItem(label = "Losses", value = botWins.toString(), color = Color(0xFFEF4444))
                    StatMetricItem(label = "Draws", value = draws.toString(), color = AmberAccent)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ratio Bar
                val totalDecisive = maxOf(1, gamesCount)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    if (playerWins > 0) {
                        Box(
                            modifier = Modifier
                                .weight((playerWins.toFloat() / totalDecisive))
                                .fillMaxSize()
                                .background(EmeraldAccent)
                        )
                    }
                    if (draws > 0) {
                        Box(
                            modifier = Modifier
                                .weight((draws.toFloat() / totalDecisive))
                                .fillMaxSize()
                                .background(AmberAccent)
                        )
                    }
                    if (botWins > 0) {
                        Box(
                            modifier = Modifier
                                .weight((botWins.toFloat() / totalDecisive))
                                .fillMaxSize()
                                .background(Color(0xFFEF4444))
                        )
                    }
                    if (gamesCount == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SurfaceHighlight)
                        )
                    }
                }
            }
        }

        // Memory & Moves Catalog Card
        Card(
            modifier = Modifier.fillMaxWidth().testTag("memory_stats_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Neural Memory",
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Learned Habits & Memory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Player Moves Indexed", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "$totalMovesRecorded moves", fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Favorite Piece Breakdown
                Text(
                    text = "Piece Activation Distribution",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (pieceDistribution.isEmpty()) {
                    Text(
                        text = "Play your first game to generate your piece preference profile.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    val maxCount = maxOf(1, pieceDistribution.maxOfOrNull { it.count } ?: 1)
                    pieceDistribution.take(5).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = item.pieceType.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier.size(70.dp, 16.dp),
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            LinearProgressIndicator(
                                progress = { (item.count.toFloat() / maxCount) },
                                modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = CyanAccent,
                                trackColor = SurfaceHighlight
                            )
                            Text(
                                text = "${item.count}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}
