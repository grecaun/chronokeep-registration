package com.chronokeep.registration.objects.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DatabaseSettingDao {
    @Query("SELECT * FROM setting WHERE name=:name")
    fun getSetting(name: String): DatabaseSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSetting(setting: DatabaseSetting)

    @Delete
    fun deleteSetting(setting: DatabaseSetting)
}