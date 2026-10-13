package com.example.domain

import com.example.data.GameRecordDao
import com.example.data.GameRecordEntity
import com.example.data.OpeningCount
import com.example.data.PieceCount
import com.example.data.PlayerMoveDao
import com.example.data.PlayerMoveEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class PlayerStyleProfile(
    val totalMovesRecorded: Int,
    val totalPositionsRecorded: Int,
    val aggressionRating: Int, // 0 - 100%
    val favoritePiece: String,
    val mirrorAccuracyPercent: Int
)

/**
 * Manages player move persistence and calculates stylistic habits
 * (piece bias, aggression, opening repertoire, mirror accuracy).
 */
class PlayerModelRepository(
    private val playerMoveDao: PlayerMoveDao,
    private val gameRecordDao: GameRecordDao
) {

    val allGames: Flow<List<GameRecordEntity>> = gameRecordDao.getAllGames()
    val gamesCount: Flow<Int> = gameRecordDao.getGamesCount()
    val playerWins: Flow<Int> = gameRecordDao.getPlayerWinsCount()
    val botWins: Flow<Int> = gameRecordDao.getBotWinsCount()
    val draws: Flow<Int> = gameRecordDao.getDrawsCount()
    val openingStats: Flow<List<OpeningCount>> = gameRecordDao.getOpeningsSummary()
    val piecePreferences: Flow<List<PieceCount>> = playerMoveDao.getAllPieceDistribution()

    val totalPlayerMovesCount: Flow<Int> = playerMoveDao.getPlayerMovesCount()
    val distinctFensCount: Flow<Int> = playerMoveDao.getDistinctFensCount()

    /**
     * Calculates the overall Mirror Accuracy percentage:
     * (Total matched mirror moves made by the bot / total bot moves) * 100
     */
    val mirrorAccuracy: Flow<Int> = combine(
        gameRecordDao.getTotalMatchedMirrorMoves(),
        gameRecordDao.getTotalMovesPlayed()
    ) { matched, total ->
        val m = matched ?: 0
        val t = total ?: 0
        if (t > 0) {
            // Half the total moves were bot turns on average
            val botMoves = maxOf(1, t / 2)
            minOf(100, ((m * 100) / botMoves))
        } else {
            0
        }
    }

    suspend fun savePlayerMoves(moves: List<PlayerMoveEntity>) {
        if (moves.isNotEmpty()) {
            playerMoveDao.insertMoves(moves)
        }
    }

    suspend fun saveGameRecord(game: GameRecordEntity) {
        gameRecordDao.insertGame(game)
    }

    suspend fun getLastGame(): GameRecordEntity? {
        return gameRecordDao.getLastGame()
    }

    suspend fun getMovesForPosition(normalizedFen: String, color: String): List<PlayerMoveEntity> {
        return playerMoveDao.getMovesForFen(normalizedFen, color)
    }

    suspend fun getPiecePreferences(color: String): Map<String, Float> {
        val list = playerMoveDao.getPieceDistribution(color)
        val total = list.sumOf { it.count }.toFloat()
        if (total == 0f) return emptyMap()
        return list.associate { it.pieceType to (it.count / total) }
    }

    suspend fun resetAllStyleData() {
        playerMoveDao.deleteAllMoves()
        gameRecordDao.deleteAllGames()
    }
}
