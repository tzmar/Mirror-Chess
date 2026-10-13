package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameRecordEntity)

    @Query("SELECT * FROM game_records ORDER BY gameIndex DESC")
    fun getAllGames(): Flow<List<GameRecordEntity>>

    @Query("SELECT COUNT(*) FROM game_records")
    fun getGamesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE result = 'PLAYER_WON'")
    fun getPlayerWinsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE result = 'BOT_WON'")
    fun getBotWinsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM game_records WHERE result = 'DRAW'")
    fun getDrawsCount(): Flow<Int>

    @Query("SELECT * FROM game_records ORDER BY gameIndex DESC LIMIT 1")
    suspend fun getLastGame(): GameRecordEntity?

    @Query("SELECT SUM(matchedMirrorMovesCount) FROM game_records")
    fun getTotalMatchedMirrorMoves(): Flow<Int?>

    @Query("SELECT SUM(movesCount) FROM game_records")
    fun getTotalMovesPlayed(): Flow<Int?>

    @Query("SELECT openingName, playerColor as color, COUNT(*) as count FROM game_records GROUP BY openingName, playerColor ORDER BY count DESC")
    fun getOpeningsSummary(): Flow<List<OpeningCount>>

    @Query("DELETE FROM game_records")
    suspend fun deleteAllGames()
}
