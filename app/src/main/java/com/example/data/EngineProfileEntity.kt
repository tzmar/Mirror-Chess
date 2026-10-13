package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Metadata for built-in, downloaded, or imported engine models/profiles.
 */
@Entity(tableName = "engine_profiles")
data class EngineProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val version: String,
    val sizeBytes: Long,
    val checksum: String,
    val localFilePath: String? = null,
    val isBuiltIn: Boolean = false,
    val isDownloaded: Boolean = false,
    val isImported: Boolean = false,
    val downloadUrl: String? = null,
    val description: String = ""
)
