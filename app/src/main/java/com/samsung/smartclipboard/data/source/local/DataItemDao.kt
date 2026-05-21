package com.samsung.smartclipboard.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.samsung.smartclipboard.data.model.DataItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataItemDao {
    @Query("SELECT * FROM data_items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DataItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DataItemEntity): Long

    @Query("DELETE FROM data_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE data_items SET createdAt = :createdAt WHERE content = :content AND type = :type")
    suspend fun updateCreatedAtByContentAndType(content: String, type: String, createdAt: Long)

    @Query("DELETE FROM data_items")
    suspend fun clearAll()
}
