package com.kairos.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WordEntity::class, QuoteEntity::class, ExtraEntity::class, DayEntity::class, MoodEntity::class,
        CapsuleEntity::class, AttachmentEntity::class, MomentEntity::class,
    ],
    version = 1,
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

    companion object {
        fun create(context: Context): KairosDatabase =
            Room.databaseBuilder(context, KairosDatabase::class.java, "kairos.db").build()

        fun inMemory(context: Context): KairosDatabase =
            Room.inMemoryDatabaseBuilder(context, KairosDatabase::class.java).allowMainThreadQueries().build()
    }
}
