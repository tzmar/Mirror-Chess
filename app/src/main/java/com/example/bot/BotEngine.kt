package com.example.bot

import com.example.domain.ChessRepository
import com.example.domain.PlayerModelRepository
import com.example.engine.EngineManager
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

data class BotMoveDecision(
    val move: Move,
    val isMirrorMatch: Boolean,
    val explanation: String
)

/**
 * Executes bot decisions according to the Mirror Chess learning algorithm:
 * - Game 1: ~400 Elo baseline bot (legal moves, frequent blunders).
 * - Game 2+: Positional lookup in player's history. If found, replays the player's
 *   most frequent historical move. Otherwise, falls back to a habit-weighted choice
 *   using the selected engine as base.
 */
class BotEngine(
    private val chessRepository: ChessRepository,
    private val playerModelRepository: PlayerModelRepository,
    private val engineManager: EngineManager
) {

    suspend fun getBotMove(
        isFirstGame: Boolean,
        botColor: Side,
        currentElo: Int
    ): BotMoveDecision? = withContext(Dispatchers.Default) {
        val legalMoves = chessRepository.getLegalMoves()
        if (legalMoves.isEmpty()) return@withContext null

        val currentFen = chessRepository.currentFen
        val normalizedFen = chessRepository.normalizeFen(currentFen)
        val colorString = if (botColor == Side.WHITE) "WHITE" else "BLACK"

        // Game 1: Baseline bot (~400 Elo, legal moves, frequent blunders)
        if (isFirstGame) {
            val engine = engineManager.getActiveEngine()
            engine.setStrength(400)
            engine.setPosition(currentFen)
            val engineUci = engine.getBestMove(depth = 1, moveTimeMs = 400)
            val chosenMove = legalMoves.firstOrNull { it.toString().lowercase() == engineUci?.lowercase() }
                ?: legalMoves.random()

            return@withContext BotMoveDecision(
                move = chosenMove,
                isMirrorMatch = false,
                explanation = "Baseline Novice (400 Elo)"
            )
        }

        // Game 2+: Step 1 - Check player's saved move database for the bot's color in this position
        val recordedMoves = playerModelRepository.getMovesForPosition(normalizedFen, colorString)

        if (recordedMoves.isNotEmpty()) {
            // Find the most frequent move played by the user in this exact position
            val moveFrequencies = recordedMoves.groupingBy { it.moveUci.lowercase() }.eachCount()
            val mostFrequentUci = moveFrequencies.maxByOrNull { it.value }?.key

            val matchedMove = legalMoves.firstOrNull { it.toString().lowercase() == mostFrequentUci }
            if (matchedMove != null) {
                val times = moveFrequencies[mostFrequentUci] ?: 1
                return@withContext BotMoveDecision(
                    move = matchedMove,
                    isMirrorMatch = true,
                    explanation = "Direct Mirror: You played this $times time${if (times > 1) "s" else ""}"
                )
            }
        }

        // Step 2: Position not in database - Fall back to habit-weighted choice
        val pieceHabits = playerModelRepository.getPiecePreferences(colorString)
        val engine = engineManager.getActiveEngine()
        engine.setStrength(currentElo)
        engine.setPosition(currentFen)

        // Evaluate baseline best move from UCI engine
        val engineBestUci = engine.getBestMove(depth = 3, moveTimeMs = 600)
        val engineCandidate = legalMoves.firstOrNull { it.toString().lowercase() == engineBestUci?.lowercase() }

        // Weight all legal candidate moves by matching player habits:
        // - Favored piece type frequency
        // - Aggressive capture preference
        // - Checks given
        val scoredMoves = legalMoves.map { move ->
            var weight = 1.0f

            // Baseline engine endorsement
            if (move == engineCandidate) {
                weight += 3.0f
            }

            // Piece preference weight
            val piece = chessRepository.getPiece(move.from)
            val pieceHabitScore = pieceHabits[piece.pieceType.name] ?: 0.1f
            weight += (pieceHabitScore * 4.0f)

            // Aggression: Captures
            val target = chessRepository.getPiece(move.to)
            if (target != Piece.NONE) {
                weight += 2.0f
            }

            // Central control habit (e4, d4, e5, d5)
            val toRank = move.to.rank.ordinal
            val toFile = move.to.file.ordinal
            if (toRank in 3..4 && toFile in 3..4) {
                weight += 1.2f
            }

            move to weight
        }

        // Weighted probabilistic selection
        val totalWeight = scoredMoves.sumOf { it.second.toDouble() }
        var randomPoint = Random.nextDouble() * totalWeight
        var selectedMove = scoredMoves.first().first

        for ((move, weight) in scoredMoves) {
            randomPoint -= weight
            if (randomPoint <= 0) {
                selectedMove = move
                break
            }
        }

        BotMoveDecision(
            move = selectedMove,
            isMirrorMatch = false,
            explanation = "Habit Mirror: Matched piece & aggression tendencies"
        )
    }
}
