package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores historical game results, opening names, and mirror alignment statistics.
 */
@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey
    val id: String,
    val gameIndex: Int, // 1 for Game 1, 2 for Game 2, etc.
    val playerColor: String, // "WHITE" or "BLACK"
    val botColor: String, // "BLACK" or "WHITE"
    val result: String, // "PLAYER_WON", "BOT_WON", "DRAW"
    val termination: String, // "CHECKMATE", "RESIGNATION", "STALEMATE", "DRAW_INSUFFICIENT", "DRAW_FIFTY_MOVES", "DRAW_REPETITION"
    val movesCount: Int,
    val matchedMirrorMovesCount: Int, // How many bot moves matched the player's saved moves
    val openingName: String,
    val timestamp: Long = System.currentTimeMillis()
)
