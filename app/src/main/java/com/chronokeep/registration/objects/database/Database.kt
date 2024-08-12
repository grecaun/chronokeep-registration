package com.chronokeep.registration.objects.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DatabaseParticipant::class, DatabaseSetting::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun participantDao(): DatabaseParticipantDao
    abstract fun settingDao(): DatabaseSettingDao
}