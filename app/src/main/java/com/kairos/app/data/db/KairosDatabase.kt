package com.kairos.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WordEntity::class, QuoteEntity::class, DayEntity::class, MoodEntity::class,
        CapsuleEntity::class, AttachmentEntity::class, JoyDropEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KairosDatabase : RoomDatabase() {
    abstract fun words(): WordDao
    abstract fun quotes(): QuoteDao
    abstract fun days(): DayDao
    abstract fun moods(): MoodDao
    abstract fun capsules(): CapsuleDao
    abstract fun joy(): JoyDao

    companion object {
        fun create(context: Context): KairosDatabase =
            // A new file name: the rebuilt app starts clean instead of migrating the old schema.
            Room.databaseBuilder(context, KairosDatabase::class.java, "kairos-v2.db").build()
    }
}
