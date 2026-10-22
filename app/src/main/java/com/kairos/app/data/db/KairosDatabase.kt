package com.kairos.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WordEntity::class, QuoteEntity::class, ExtraEntity::class, DayEntity::class, MoodEntity::class,
        CapsuleEntity::class, AttachmentEntity::class, MomentEntity::class, BookEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class KairosDatabase : RoomDatabase() {
    abstract fun words(): WordDao
    abstract fun quotes(): QuoteDao
    abstract fun extras(): ExtraDao
    abstract fun days(): DayDao
    abstract fun moods(): MoodDao
    abstract fun capsules(): CapsuleDao
    abstract fun moments(): MomentDao
    abstract fun books(): BookDao

    companion object {
        fun create(context: Context): KairosDatabase =
            Room.databaseBuilder(context, KairosDatabase::class.java, "kairos.db").addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

        fun inMemory(context: Context): KairosDatabase =
            Room.inMemoryDatabaseBuilder(context, KairosDatabase::class.java).allowMainThreadQueries().build()
    }
}

/** v2: borrowed-word languages, quote kinds and sources, and micro-summaries of books. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `words` ADD COLUMN `language` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `quotes` ADD COLUMN `kind` TEXT NOT NULL DEFAULT 'quote'")
        db.execSQL("ALTER TABLE `quotes` ADD COLUMN `source` TEXT NOT NULL DEFAULT ''")
        db.execSQL("CREATE TABLE IF NOT EXISTS `books` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `author` TEXT NOT NULL, `year` INTEGER NOT NULL, `summary` TEXT NOT NULL, `ideas` TEXT NOT NULL, `line` TEXT NOT NULL, `hook` TEXT NOT NULL, `category` TEXT NOT NULL, `minutes` INTEGER NOT NULL, `generatedAt` INTEGER NOT NULL, `saved` INTEGER NOT NULL, `savedAt` INTEGER, `timesShown` INTEGER NOT NULL, `lastShownDay` INTEGER, `taste` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_books_category` ON `books` (`category`)")
    }
}

/** v3: dwell time and shares, the Feed's quiet signals. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `words` ADD COLUMN `dwellMs` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `words` ADD COLUMN `shares` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `quotes` ADD COLUMN `dwellMs` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `quotes` ADD COLUMN `shares` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `books` ADD COLUMN `dwellMs` INTEGER NOT NULL DEFAULT 0")
    }
}
