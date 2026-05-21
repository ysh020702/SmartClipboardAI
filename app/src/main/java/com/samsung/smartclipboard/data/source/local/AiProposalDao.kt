package com.samsung.smartclipboard.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samsung.smartclipboard.data.model.AiProposalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiProposalDao {
    @Query("SELECT * FROM ai_proposals ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AiProposalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AiProposalEntity): Long

    @Query("DELETE FROM ai_proposals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM ai_proposals")
    suspend fun clearAll()
}