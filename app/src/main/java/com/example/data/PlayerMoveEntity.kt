package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores each move made by the player to power the mirror bot.
 * Indexed on fenBefore and color for fast positional lookups during games.
 */
@Entity(
    tableName = "player_moves",
    indices = [
        Index(value = ["fenBefore", "color"]),
        Index(value = ["gameId"])
    ]
)
data class PlayerMoveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: String,
    val moveNumber: Int,
    val color: String, // "WHITE" or "BLACK"
    val fenBefore: String, // Normalized FEN (piece placement + side to move + castling + ep)
    val moveUci: String, // UCI string, e.g. "e2e4"
    val san: String, // Standard Algebraic Notation, e.g. "e4"
    val pieceType: String, // "PAWN", "KNIGHT", "BISHOP", "ROOK", "QUEEN", "KING"
    val isCapture: Boolean,
    val isCheck: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
