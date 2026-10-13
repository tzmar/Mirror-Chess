package com.example.engine

/**
 * Standardized interface for UCI-compliant chess engines.
 * Enables interchangeable engines without changing game or bot logic.
 */
interface ChessEngine {
    val id: String
    val name: String
    val version: String
    val description: String
    val isBuiltIn: Boolean

    suspend fun start(): Boolean
    suspend fun startNewGame()
    suspend fun setStrength(elo: Int)
    suspend fun setPosition(fen: String, moves: List<String> = emptyList())
    suspend fun getBestMove(depth: Int = 8, moveTimeMs: Long = 1000): String?
    suspend fun stop()
    suspend fun sendCommand(command: String): List<String>
    fun close()
}
