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
        AutoMigration (from = 3, to = 4)
    ]
)
abstract class Database : RoomDatabase() {
    abstract fun participantDao(): DatabaseParticipantDao
    abstract fun settingDao(): DatabaseSettingDao
}

val migration_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE participant RENAME TO participant_old;")
        database.execSQL("UPDATE participant_old SET registration_id = first_name || last_name || distance || birthdate || chronokeep_info WHERE registration_id='';")
        database.execSQL("CREATE TABLE IF NOT EXISTS participant (row_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, registration_id TEXT NOT NULL, bib TEXT NOT NULL, first_name TEXT NOT NULL, last_name TEXT NOT NULL, birthdate TEXT NOT NULL, gender TEXT NOT NULL, distance TEXT NOT NULL, mobile TEXT NOT NULL, sms_enabled INTEGER NOT NULL, apparel TEXT NOT NULL, updated_at INTEGER NOT NULL DEFAULT 0, chronokeep_info TEXT NOT NULL DEFAULT '', uploaded INTEGER NOT NULL DEFAULT false)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_participant_first_name_last_name_distance_birthdate_chronokeep_info ON participant (first_name, last_name, distance, birthdate, chronokeep_info)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_participant_registration_id ON participant (registration_id)")
        database.execSQL("INSERT OR IGNORE INTO participant (row_id, registration_id, bib, first_name, last_name, birthdate, gender, distance, mobile, sms_enabled, apparel, updated_at, chronokeep_info, uploaded) SELECT row_id, registration_id, bib, first_name, last_name, birthdate, gender, distance, mobile, sms_enabled, apparel, updated_at, chronokeep_info, uploaded FROM participant_old;")
        database.execSQL("DROP TABLE participant_old;")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_participant_first_name_last_name_distance_birthdate_chronokeep_info ON participant (first_name, last_name, distance, birthdate, chronokeep_info)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_participant_registration_id ON participant (registration_id)")
    }
}