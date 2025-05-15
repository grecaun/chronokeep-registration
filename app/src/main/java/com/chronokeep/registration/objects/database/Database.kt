package com.chronokeep.registration.objects.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DatabaseParticipant::class, DatabaseSetting::class],
    version = 4,
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