package com.example.domain

import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move

enum class GameResult {
    IN_PROGRESS,
    WHITE_WON_CHECKMATE,
    BLACK_WON_CHECKMATE,
    DRAW_STALEMATE,
    DRAW_INSUFFICIENT,
    DRAW_FIFTY_MOVES,
    DRAW_REPETITION,
    RESIGNATION_WHITE,
    RESIGNATION_BLACK
}

data class ChessMoveRecord(
    val moveUci: String,
    val san: String,
    val fenBefore: String,
    val pieceType: PieceType,
    val isCapture: Boolean,
    val isCheck: Boolean,
    val isMirrorMatch: Boolean = false
)

/**
 * Repository wrapping chesslib Board rules, move validation, SAN generation,
 * and opening identification.
 */
class ChessRepository {

    private var board = Board()
    private val moveHistory = mutableListOf<ChessMoveRecord>()

    val currentBoard: Board
        get() = board

    val currentFen: String
        get() = board.fen

    val sideToMove: Side
        get() = board.sideToMove

    fun resetBoard(fen: String? = null) {
        board = Board()
        if (fen != null) {
            board.loadFromFen(fen)
        }
        moveHistory.clear()
    }

    fun getLegalMoves(): List<Move> {
        return board.legalMoves()
    }

    fun getLegalMovesForSquare(square: Square): List<Move> {
        return board.legalMoves().filter { it.from == square }
    }

    fun getPiece(square: Square): Piece {
        return board.getPiece(square)
    }

    fun isKingInCheck(): Boolean {
        return board.isKingAttacked
    }

    fun checkGameStatus(): GameResult {
        return when {
            board.isMated -> {
                if (board.sideToMove == Side.WHITE) GameResult.BLACK_WON_CHECKMATE
                else GameResult.WHITE_WON_CHECKMATE
            }
            board.isStaleMate -> GameResult.DRAW_STALEMATE
            board.isInsufficientMaterial -> GameResult.DRAW_INSUFFICIENT
            board.isDraw -> GameResult.DRAW_FIFTY_MOVES
            else -> GameResult.IN_PROGRESS
        }
    }

    /**
     * Executes a move on the board and logs the historical record.
     */
    fun makeMove(move: Move, isMirrorMatch: Boolean = false): ChessMoveRecord? {
        val fenBefore = normalizeFen(board.fen)
        val piece = board.getPiece(move.from)
        val targetPiece = board.getPiece(move.to)
        val isCapture = targetPiece != Piece.NONE || isEnPassantCapture(move)
        val san = generateSan(move)

        board.doMove(move)

        val isCheck = board.isKingAttacked
        val record = ChessMoveRecord(
            moveUci = move.toString().lowercase(),
            san = san,
            fenBefore = fenBefore,
            pieceType = piece.pieceType,
            isCapture = isCapture,
            isCheck = isCheck,
            isMirrorMatch = isMirrorMatch
        )
        moveHistory.add(record)
        return record
    }

    fun getHistory(): List<ChessMoveRecord> {
        return moveHistory.toList()
    }

    /**
     * Normalizes FEN by keeping board position, active color, castling rights, and ep square,
     * removing the halfmove clock and fullmove number so identical positions match regardless of turn number.
     */
    fun normalizeFen(fen: String): String {
        val parts = fen.trim().split("\\s+".toRegex())
        return if (parts.size >= 4) {
            "${parts[0]} ${parts[1]} ${parts[2]} ${parts[3]}"
        } else {
            fen
        }
    }

    private fun isEnPassantCapture(move: Move): Boolean {
        val piece = board.getPiece(move.from)
        return piece.pieceType == PieceType.PAWN &&
                move.to == board.enPassant &&
                move.from.file != move.to.file
    }

    /**
     * Generates clean Standard Algebraic Notation (SAN).
     */
    fun generateSan(move: Move): String {
        val piece = board.getPiece(move.from)
        val target = board.getPiece(move.to)
        val isCapture = target != Piece.NONE || isEnPassantCapture(move)

        // Castling
        if (piece.pieceType == PieceType.KING) {
            if (move.from == Square.E1 && move.to == Square.G1) return "O-O"
            if (move.from == Square.E1 && move.to == Square.C1) return "O-O-O"
            if (move.from == Square.E8 && move.to == Square.G8) return "O-O"
            if (move.from == Square.E8 && move.to == Square.C8) return "O-O-O"
        }

        val piecePrefix = when (piece.pieceType) {
            PieceType.PAWN -> ""
            PieceType.KNIGHT -> "N"
            PieceType.BISHOP -> "B"
            PieceType.ROOK -> "R"
            PieceType.QUEEN -> "Q"
            PieceType.KING -> "K"
            else -> ""
        }

        val dest = move.to.toString().lowercase()

        return if (piece.pieceType == PieceType.PAWN) {
            val pawnBase = if (isCapture) {
                "${move.from.toString().lowercase().first()}x$dest"
            } else {
                dest
            }
            if (move.promotion != Piece.NONE) {
                "${pawnBase}=${move.promotion.pieceType.name.first()}"
            } else {
                pawnBase
            }
        } else {
            val captureMark = if (isCapture) "x" else ""
            "$piecePrefix$captureMark$dest"
        }
    }

    /**
     * Identifies chess openings based on standard move sequences.
     */
    fun identifyOpening(moves: List<String>): String {
        if (moves.isEmpty()) return "Initial Position"
        val firstMove = moves.getOrNull(0) ?: ""
        val secondMove = moves.getOrNull(1) ?: ""
        val thirdMove = moves.getOrNull(2) ?: ""
        val fourthMove = moves.getOrNull(3) ?: ""

        return when {
            firstMove == "e2e4" && secondMove == "c7c5" -> "Sicilian Defense"
            firstMove == "e2e4" && secondMove == "e7e6" -> "French Defense"
            firstMove == "e2e4" && secondMove == "c7c6" -> "Caro-Kann Defense"
            firstMove == "e2e4" && secondMove == "e7e5" && thirdMove == "g1f3" && fourthMove == "b8c6" -> "Open Game (e4 e5)"
            firstMove == "d2d4" && secondMove == "d7d5" && thirdMove == "c2c4" -> "Queen's Gambit"
            firstMove == "d2d4" && secondMove == "g8f6" && thirdMove == "c2c4" && fourthMove == "g7g6" -> "King's Indian Defense"
            firstMove == "d2d4" && secondMove == "g8f6" -> "Indian Defense"
            firstMove == "c2c4" -> "English Opening"
            firstMove == "g1f3" -> "Réti Opening"
            firstMove == "e2e4" -> "King's Pawn Opening"
            firstMove == "d2d4" -> "Queen's Pawn Opening"
            else -> "Modern / Flank Opening"
        }
    }
}
