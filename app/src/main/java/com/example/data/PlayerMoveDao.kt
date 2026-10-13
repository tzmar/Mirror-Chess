package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class PieceCount(
    val pieceType: String,
    val count: Int
)

data class OpeningCount(
    val openingName: String,
    val color: String,
    val count: Int
)

@Dao
interface PlayerMoveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoves(moves: List<PlayerMoveEntity>)

    @Query("SELECT * FROM player_moves WHERE fenBefore = :fenBefore AND color = :color ORDER BY id ASC")
    suspend fun getMovesForFen(fenBefore: String, color: String): List<PlayerMoveEntity>

    @Query("SELECT * FROM player_moves ORDER BY timestamp DESC")
    fun getAllPlayerMoves(): Flow<List<PlayerMoveEntity>>

    @Query("SELECT COUNT(*) FROM player_moves")
    fun getPlayerMovesCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT fenBefore) FROM player_moves")
    fun getDistinctFensCount(): Flow<Int>

    @Query("SELECT pieceType, COUNT(*) as count FROM player_moves WHERE color = :color GROUP BY pieceType ORDER BY count DESC")
    suspend fun getPieceDistribution(color: String): List<PieceCount>

    @Query("SELECT pieceType, COUNT(*) as count FROM player_moves GROUP BY pieceType ORDER BY count DESC")
    fun getAllPieceDistribution(): Flow<List<PieceCount>>

    @Query("SELECT COUNT(*) FROM player_moves WHERE isCapture = 1")
    fun getCaptureCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM player_moves WHERE isCheck = 1")
    fun getCheckCount(): Flow<Int>

    @Query("DELETE FROM player_moves")
    suspend fun deleteAllMoves()
}
