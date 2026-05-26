package com.samsung.smartclipboard

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

enum class InputType { OCR, URL, TEXT }
class RoomConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        value?.joinToString("||") ?: ""
    @TypeConverter
    fun toStringList(value: String?): List<String> =
        value?.split("||")?.filter { it.isNotBlank() } ?: emptyList()
}
@Entity(tableName = "knowledge_table")
data class KnowledgeEntity(
    @PrimaryKey val id: String,
    val type: InputType,
    val source: String,
    val title: String,
    val topic: String,
    val purpose: String,
    val summary: String,
    val keywords: List<String>,
    val content: String,
    val groupKey: String,
    val groupReason: String,
    val createdAt: Long
)

@Dao
interface KnowledgeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: KnowledgeEntity)
    @Query("SELECT * FROM knowledge_table ORDER BY createdAt DESC")
    suspend fun getAll(): List<KnowledgeEntity>
}

@Database(entities = [KnowledgeEntity::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): KnowledgeDao
}

@Serializable
data class LlmStructuredOutput(
    val title: String,
    val topic: String,
    val purpose: String,
    val summary: String,
    val keywords: List<String>,
    val cleanedContent: String,
    val groupKey: String,
    val groupReason: String
)