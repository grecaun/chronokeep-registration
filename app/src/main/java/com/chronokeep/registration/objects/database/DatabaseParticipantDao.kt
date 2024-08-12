package com.chronokeep.registration.objects.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface DatabaseParticipantDao {
    @Query("SELECT * FROM participant")
    fun getParticipants(): List<DatabaseParticipant>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addParticipants(parts: List<DatabaseParticipant>)

    @Update
    fun updateParticipants(parts: List<DatabaseParticipant>)

    @Delete
    fun deleteParticipants(parts: List<DatabaseParticipant>)
}