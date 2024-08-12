package com.chronokeep.registration.objects.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="setting")
class DatabaseSetting (
    @PrimaryKey val name: String,
    @ColumnInfo val value: String,
)