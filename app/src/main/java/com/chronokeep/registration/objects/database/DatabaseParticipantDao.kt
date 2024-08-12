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
    fun getAllParticipants(): List<DatabaseParticipant>

    @Query("SELECT * FROM participant WHERE event_slug = :slug AND event_year = :year")
    fun getEventParticipants(slug: String, year: String): List<DatabaseParticipant>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addParticipants(parts: List<DatabaseParticipant>)

    @Update
    fun updateParticipants(parts: List<DatabaseParticipant>)

    @Delete
    fun deleteParticipants(parts: List<DatabaseParticipant>)
}