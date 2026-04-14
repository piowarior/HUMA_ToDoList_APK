package com.huma.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommitmentDao {
    @Query("SELECT * FROM commitments ORDER BY createdAt DESC")
    fun getAllCommitments(): Flow<List<CommitmentEntity>>

    @Insert
    suspend fun insertCommitment(commitment: CommitmentEntity): Long

    @Update
    suspend fun updateCommitment(commitment: CommitmentEntity)

    @Delete
    suspend fun deleteCommitment(commitment: CommitmentEntity)

    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getCommitmentById(id: Int): CommitmentEntity?
}
