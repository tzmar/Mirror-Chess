package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlayerMoveEntity::class,
        GameRecordEntity::class,
        EngineProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerMoveDao(): PlayerMoveDao
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun engineProfileDao(): EngineProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mirror_chess.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default engine profiles
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.engineProfileDao()?.insertOrUpdate(
                                    EngineProfileEntity(
                                        id = "builtin_stockfish",
                                        name = "Stockfish Embedded (UCI)",
                                        version = "16.1 Native Mini",
                                        sizeBytes = 2_450_000L,
                                        checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                                        isBuiltIn = true,
                                        isDownloaded = true,
                                        description = "Offline UCI chess engine bundled directly inside the app with adaptive Elo scaling."
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
