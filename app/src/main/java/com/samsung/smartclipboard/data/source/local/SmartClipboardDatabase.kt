package com.samsung.smartclipboard.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.samsung.smartclipboard.data.model.DataItemEntity
import com.samsung.smartclipboard.data.model.TopicActionEntity
import com.samsung.smartclipboard.data.model.TopicAnalysisEntity
import com.samsung.smartclipboard.data.model.TopicEntity
import com.samsung.smartclipboard.data.model.TopicItemCrossRefEntity

@Database(
    entities = [
        DataItemEntity::class,
        TopicEntity::class,
        TopicItemCrossRefEntity::class,
        TopicAnalysisEntity::class,
        TopicActionEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(KeywordConverters::class)
abstract class SmartClipboardDatabase : RoomDatabase() {
    abstract fun dataItemDao(): DataItemDao
    abstract fun topicDao(): TopicDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS topics (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_topics_title
                    ON topics(title)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS topic_item_cross_refs (
                        topicId INTEGER NOT NULL,
                        itemId INTEGER NOT NULL,
                        addedAt INTEGER NOT NULL,
                        addedBy TEXT NOT NULL,
                        PRIMARY KEY(topicId, itemId),
                        FOREIGN KEY(topicId) REFERENCES topics(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(itemId) REFERENCES data_items(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_topic_item_cross_refs_topicId
                    ON topic_item_cross_refs(topicId)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_topic_item_cross_refs_itemId
                    ON topic_item_cross_refs(itemId)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS topic_analysis_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        topicId INTEGER NOT NULL,
                        summary TEXT NOT NULL,
                        keyPoints TEXT NOT NULL,
                        sourceItemIds TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(topicId) REFERENCES topics(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_topic_analysis_results_topicId
                    ON topic_analysis_results(topicId)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS topic_actions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        topicId INTEGER NOT NULL,
                        analysisResultId INTEGER,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        status TEXT NOT NULL,
                        editablePayload TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(topicId) REFERENCES topics(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(analysisResultId) REFERENCES topic_analysis_results(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_topic_actions_topicId
                    ON topic_actions(topicId)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_topic_actions_analysisResultId
                    ON topic_actions(analysisResultId)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE data_items ADD COLUMN extractedContent TEXT"
                )
            }
        }
    }
}