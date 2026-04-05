package com.huma.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CapsuleDao {
    @Query("SELECT * FROM time_capsules ORDER BY createdAt DESC")
    fun getAllCapsules(): Flow<List<CapsuleEntity>>

    @Insert
    suspend fun insertCapsule(capsule: CapsuleEntity)

    @Query("DELETE FROM time_capsules WHERE id = :id")
    suspend fun deleteCapsule(id: Int)

    @Query("UPDATE time_capsules SET isOpened = 1 WHERE id = :id")
    suspend fun markAsOpened(id: Int)
}
