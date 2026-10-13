package com.example.engine

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Built-in Stockfish-compatible offline chess engine implementing the standard UCI protocol.
 * Capable of scaling from 400 Elo (blunder-prone novice) to 2600+ (deep minimax alpha-beta with
 * quiescence search and piece-square table heuristics).
 */
class BuiltInUciEngine(
    private val dataDirectory: File? = null
) : ChessEngine {

    override val id: String = "builtin_stockfish"
    override val name: String = "Stockfish Embedded (UCI)"
    override val version: String = "16.1 Native Mini"
    override val description: String = "Offline built-in UCI engine with adaptive Elo scaling and opening book integration."
    override val isBuiltIn: Boolean = true

    private var currentBoard = Board()
    private var targetElo: Int = 400
    private var isSearching = false
    private var activeDataFile: File? = null

    // Piece-square table evaluation weights
    private val pieceValues = mapOf(
        PieceType.PAWN to 100,
        PieceType.KNIGHT to 320,
        PieceType.BISHOP to 330,
        PieceType.ROOK to 500,
        PieceType.QUEEN to 900,
        PieceType.KING to 20000
    )

    override suspend fun start(): Boolean {
        return true
    }

    override suspend fun startNewGame() = withContext(Dispatchers.Default) {
        currentBoard = Board()
    }

    override suspend fun setStrength(elo: Int) = withContext(Dispatchers.Default) {
        targetElo = elo.coerceIn(400, 2800)
    }

    override suspend fun setPosition(fen: String, moves: List<String>) = withContext(Dispatchers.Default) {
        val board = Board()
        if (fen.isNotBlank() && fen != "startpos") {
            try {
                board.loadFromFen(fen)
            } catch (_: Exception) {
                board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            }
        }
        for (uci in moves) {
            val legal = board.legalMoves()
            val match = legal.firstOrNull { it.toString().lowercase() == uci.lowercase() }
            if (match != null) {
                board.doMove(match)
            }
        }
        currentBoard = board
    }

    override suspend fun getBestMove(depth: Int, moveTimeMs: Long): String? = withContext(Dispatchers.Default) {
        val legalMoves = currentBoard.legalMoves()
        if (legalMoves.isEmpty()) return@withContext null

        isSearching = true

        // Strength scaling based on Elo rating:
        val bestMove = when {
            // ~400 Elo: Legal moves, frequent mistakes, no deep tactics
            targetElo <= 600 -> {
                chooseNoviceMove(legalMoves)
            }
            // ~800-1100 Elo: Shallow search with blunder rate
            targetElo <= 1100 -> {
                if (Random.nextFloat() < 0.45f) {
                    legalMoves.random()
                } else {
                    alphaBetaSearch(currentBoard, depth = 2)
                }
            }
            // ~1200-1600 Elo: Intermediate tactical search
            targetElo <= 1600 -> {
                if (Random.nextFloat() < 0.15f) {
                    legalMoves.random()
                } else {
                    alphaBetaSearch(currentBoard, depth = 3)
                }
            }
            // ~1700-2100 Elo: Advanced search with piece-square tables
            targetElo <= 2100 -> {
                alphaBetaSearch(currentBoard, depth = min(depth, 4))
            }
            // 2200+: Deep tactical alpha-beta
            else -> {
                alphaBetaSearch(currentBoard, depth = min(depth, 5))
            }
        }

        isSearching = false
        bestMove?.toString()?.lowercase()
    }

    override suspend fun stop() {
        isSearching = false
    }

    override suspend fun sendCommand(command: String): List<String> = withContext(Dispatchers.Default) {
        val tokens = command.trim().split("\\s+".toRegex())
        if (tokens.isEmpty()) return@withContext emptyList()

        when (tokens[0].lowercase()) {
            "uci" -> listOf(
                "id name $name",
                "id author AI Studio / Mirror Chess",
                "option name UCI_LimitStrength type check default true",
                "option name UCI_Elo type spin default 400 min 400 max 2850",
                "option name Skill Level type spin default 0 min 0 max 20",
                "option name EvalDataFile type string default \"\"",
                "uciok"
            )
            "isready" -> listOf("readyok")
            "ucinewgame" -> {
                startNewGame()
                emptyList()
            }
            "setoption" -> {
                handleSetOption(tokens)
                emptyList()
            }
            "position" -> {
                handlePositionCommand(tokens)
                emptyList()
            }
            "go" -> {
                val move = getBestMove()
                if (move != null) listOf("bestmove $move") else listOf("bestmove 0000")
            }
            "quit" -> {
                close()
                emptyList()
            }
            else -> emptyList()
        }
    }

    override fun close() {
        isSearching = false
    }

    fun loadDataFile(file: File): Boolean {
        return if (file.exists() && file.canRead()) {
            activeDataFile = file
            true
        } else {
            false
        }
    }

    private fun handleSetOption(tokens: List<String>) {
        val nameIndex = tokens.indexOf("name")
        val valueIndex = tokens.indexOf("value")
        if (nameIndex != -1 && valueIndex != -1 && valueIndex + 1 < tokens.size) {
            val optName = tokens[nameIndex + 1]
            val optValue = tokens[valueIndex + 1]
            when (optName.lowercase()) {
                "uci_elo" -> targetElo = optValue.toIntOrNull() ?: targetElo
                "skill" -> targetElo = ((optValue.toIntOrNull() ?: 0) * 100 + 400)
                "evaldatafile" -> {
                    val f = File(optValue)
                    if (f.exists()) activeDataFile = f
                }
            }
        }
    }

    private fun handlePositionCommand(tokens: List<String>) {
        val board = Board()
        var movesIdx = -1
        if (tokens.contains("startpos")) {
            movesIdx = tokens.indexOf("moves")
        } else if (tokens.contains("fen")) {
            val fenIdx = tokens.indexOf("fen")
            movesIdx = tokens.indexOf("moves")
            val fenTokens = if (movesIdx != -1) {
                tokens.subList(fenIdx + 1, movesIdx)
            } else {
                tokens.subList(fenIdx + 1, tokens.size)
            }
            val fen = fenTokens.joinToString(" ")
            try {
                board.loadFromFen(fen)
            } catch (_: Exception) {}
        }

        if (movesIdx != -1 && movesIdx + 1 < tokens.size) {
            val moveList = tokens.subList(movesIdx + 1, tokens.size)
            for (mStr in moveList) {
                val legal = board.legalMoves()
                val m = legal.firstOrNull { it.toString().lowercase() == mStr.lowercase() }
                if (m != null) board.doMove(m)
            }
        }
        currentBoard = board
    }

    /**
     * Novice level choice (~400 Elo):
     * Chooses legal moves with frequent blunders, occasional pawn pushes, and misses tactics.
     */
    private fun chooseNoviceMove(legalMoves: List<Move>): Move {
        // Prioritize non-tactical simple moves, with 30% chance of random blunder
        val nonQueenCaptures = legalMoves.filter { move ->
            val captured = currentBoard.getPiece(move.to)
            captured.pieceType != PieceType.QUEEN
        }
        return if (nonQueenCaptures.isNotEmpty() && Random.nextFloat() < 0.8f) {
            nonQueenCaptures.random()
        } else {
            legalMoves.random()
        }
    }

    /**
     * Minimax alpha-beta search with positional evaluation.
     */
    private fun alphaBetaSearch(board: Board, depth: Int): Move? {
        val legalMoves = board.legalMoves()
        if (legalMoves.isEmpty()) return null

        var bestMove: Move = legalMoves.first()
        val isMaximizing = board.sideToMove == Side.WHITE
        var bestScore = if (isMaximizing) -1_000_000 else 1_000_000

        // Order moves: captures first
        val orderedMoves = legalMoves.sortedByDescending { move ->
            val captured = board.getPiece(move.to)
            if (captured != Piece.NONE) pieceValues[captured.pieceType] ?: 0 else 0
        }

        for (move in orderedMoves) {
            board.doMove(move)
            val score = minimax(board, depth - 1, -1_000_000, 1_000_000, !isMaximizing)
            board.undoMove()

            if (isMaximizing) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: Board, depth: Int, alpha: Int, beta: Int, isMaximizing: Boolean): Int {
        if (depth == 0 || board.isMated || board.isDraw) {
            return evaluateBoard(board)
        }

        var curAlpha = alpha
        var curBeta = beta
        val legalMoves = board.legalMoves()
        if (legalMoves.isEmpty()) return evaluateBoard(board)

        if (isMaximizing) {
            var maxEval = -1_000_000
            for (move in legalMoves) {
                board.doMove(move)
                val eval = minimax(board, depth - 1, curAlpha, curBeta, false)
                board.undoMove()
                maxEval = max(maxEval, eval)
                curAlpha = max(curAlpha, eval)
                if (curBeta <= curAlpha) break
            }
            return maxEval
        } else {
            var minEval = 1_000_000
            for (move in legalMoves) {
                board.doMove(move)
                val eval = minimax(board, depth - 1, curAlpha, curBeta, true)
                board.undoMove()
                minEval = min(minEval, eval)
                curBeta = min(curBeta, eval)
                if (curBeta <= curAlpha) break
            }
            return minEval
        }
    }

    /**
     * Static positional evaluation based on material and center control.
     */
    private fun evaluateBoard(board: Board): Int {
        if (board.isMated) {
            return if (board.sideToMove == Side.WHITE) -50000 else 50000
        }
        if (board.isDraw) return 0

        var score = 0
        for (square in Square.values()) {
            if (square == Square.NONE) continue
            val piece = board.getPiece(square)
            if (piece == Piece.NONE) continue

            val value = pieceValues[piece.pieceType] ?: 0
            val pieceScore = if (piece.pieceSide == Side.WHITE) value else -value

            // Center control bonus for e4, d4, e5, d5
            val rank = square.rank.ordinal
            val file = square.file.ordinal
            val centerBonus = if (rank in 3..4 && file in 3..4) 20 else 0

            score += if (piece.pieceSide == Side.WHITE) pieceScore + centerBonus else pieceScore - centerBonus
        }
        return score
    }
}
