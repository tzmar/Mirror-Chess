package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EngineProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: EngineProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(profiles: List<EngineProfileEntity>)

    @Query("SELECT * FROM engine_profiles ORDER BY isBuiltIn DESC, name ASC")
    fun getAllProfiles(): Flow<List<EngineProfileEntity>>

    @Query("SELECT * FROM engine_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): EngineProfileEntity?

    @Query("DELETE FROM engine_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)
}
