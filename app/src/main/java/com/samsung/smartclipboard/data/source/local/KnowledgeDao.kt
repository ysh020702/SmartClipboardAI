package com.samsung.smartclipboard.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samsung.smartclipboard.data.model.KnowledgeEntity

@Dao
interface KnowledgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KnowledgeEntity)

    @Query("SELECT * FROM knowledge_table ORDER BY createdAt DESC")
    suspend fun getAll(): List<KnowledgeEntity>
}