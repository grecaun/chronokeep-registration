package com.chronokeep.registration.objects.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DatabaseParticipant::class, DatabaseSetting::class],
    version = 5,
    autoMigrations = [
        AutoMigration (from = 1, to = 2),
        AutoMigration (from = 2, to = 3),
        AutoMigration (from = 3, to = 4),
    ]
)
abstract class Database : RoomDatabase() {
    abstract fun participantDao(): DatabaseParticipantDao
    abstract fun settingDao(): DatabaseSettingDao
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DELETE FROM participant")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_participant_registration_id ON participant (registration_id)")
    }
}