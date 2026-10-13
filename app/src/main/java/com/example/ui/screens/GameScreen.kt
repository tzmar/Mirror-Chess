package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.domain.ChessMoveRecord
import com.example.domain.GameResult
import com.example.ui.GameUiState
import com.example.ui.components.ChessBoardView
import com.example.ui.components.ChessPieceView
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanAccentMuted
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square

@Composable
fun GameScreen(
    state: GameUiState,
    onSquareClick: (Square) -> Unit,
    onPieceDragged: (Square, Square) -> Unit,
    onPromotionChosen: (PieceType) -> Unit,
    onResignClick: () -> Unit,
    onNewGameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGameOver = state.gameResult != GameResult.IN_PROGRESS
    val isPlayerTurn = state.board.sideToMove == state.playerColor && !isGameOver && !state.isBotThinking

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header Banner: Game Number & Bot Identity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Game ${state.gameIndex}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Text(
                    text = if (state.gameIndex == 1) "Baseline Bot (White)" else "Mirror Bot (vs Your Style)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Mirror match badge
            if (state.isLastMoveMirrorMatch) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CyanAccentMuted.copy(alpha = 0.3f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanAccent, AmberAccent)))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mirror Match",
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "MIRROR MOVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent
                        )
                    }
                }
            }
        }

        // Bot Card (Opponent)
        PlayerStatusCard(
            name = if (state.gameIndex == 1) "Novice Bot" else "Mirror Bot",
            color = state.botColor,
            isCurrentTurn = !isPlayerTurn && !isGameOver,
            capturedPieces = if (state.botColor == Side.WHITE) state.capturedByWhite else state.capturedByBlack,
            isThinking = state.isBotThinking,
            statusSubtitle = state.botExplanation ?: if (state.isBotThinking) "Analyzing your historical tendencies..." else "Waiting",
            modifier = Modifier.fillMaxWidth()
        )

        // Chess Board Component
        ChessBoardView(
            board = state.board,
            playerColor = state.playerColor,
            selectedSquare = state.selectedSquare,
            legalMovesFromSelected = state.legalMovesFromSelected,
            lastMove = state.lastMove,
            isCheck = state.board.isKingAttacked,
            isInteractive = isPlayerTurn,
            onSquareClick = onSquareClick,
            onPieceDragged = onPieceDragged,
            onPromotionChosen = onPromotionChosen,
            pendingPromotionMove = state.pendingPromotionMove,
            modifier = Modifier.fillMaxWidth()
        )

        // Player Card (User)
        PlayerStatusCard(
            name = "You",
            color = state.playerColor,
            isCurrentTurn = isPlayerTurn,
            capturedPieces = if (state.playerColor == Side.WHITE) state.capturedByWhite else state.capturedByBlack,
            isThinking = false,
            statusSubtitle = if (isPlayerTurn) "Drag or tap a piece to move" else "Waiting for opponent",
            modifier = Modifier.fillMaxWidth()
        )

        // Game Over Banner
        AnimatedVisibility(visible = isGameOver) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_over_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceHighlight),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanAccent, AmberAccent)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.statusMessage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                    Text(
                        text = "Every game trains the bot to reflect your exact play style.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Action Buttons: Resign and New Game
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onResignClick,
                enabled = !isGameOver,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("resign_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Resign",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Resign", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onNewGameClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("new_game_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color(0xFF090D16))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "New Game",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Game", fontWeight = FontWeight.Bold)
            }
        }

        // Move History Panel (Standard Algebraic Notation)
        MoveHistoryCard(
            moves = state.moveHistory,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PlayerStatusCard(
    name: String,
    color: Side,
    isCurrentTurn: Boolean,
    capturedPieces: List<PieceType>,
    isThinking: Boolean,
    statusSubtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTurn) SurfaceHighlight else SurfaceElevated
        ),
        border = if (isCurrentTurn) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyanAccent, CyanAccentMuted))) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Color swatch
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (color == Side.WHITE) Color.White else Color(0xFF1E293B))
                        .border(1.dp, Color.Gray, CircleShape)
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (isThinking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = CyanAccent
                            )
                        }
                    }
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = if (isCurrentTurn) CyanAccent else TextSecondary
                    )
                }
            }

            // Captured pieces icons
            if (capturedPieces.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pieceOpposite = if (color == Side.WHITE) Side.BLACK else Side.WHITE
                    capturedPieces.take(6).forEach { pieceType ->
                        val dummy = Piece.make(pieceOpposite, pieceType)
                        Box(modifier = Modifier.size(16.dp)) {
                            ChessPieceView(piece = dummy)
                        }
                    }
                    if (capturedPieces.size > 6) {
                        Text(
                            text = "+${capturedPieces.size - 6}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MoveHistoryCard(
    moves: List<ChessMoveRecord>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("move_history_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Move History",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (moves.isEmpty()) {
                Text(
                    text = "No moves yet. Make your first move!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val pairs = moves.chunked(2)
                    items(pairs) { pair ->
                        val moveNum = moves.indexOf(pair[0]) / 2 + 1
                        val whiteMove = pair[0]
                        val blackMove = pair.getOrNull(1)

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceDark,
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$moveNum.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = whiteMove.san,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (whiteMove.isMirrorMatch) CyanAccent else TextPrimary
                                )
                                if (blackMove != null) {
                                    Text(
                                        text = blackMove.san,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = if (blackMove.isMirrorMatch) CyanAccent else TextPrimary
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
