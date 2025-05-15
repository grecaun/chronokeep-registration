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

    @Query("SELECT * FROM participant WHERE uploaded = 0")
    fun getNotUploaded(): List<DatabaseParticipant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addParticipants(parts: List<DatabaseParticipant>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addParticipant(part: DatabaseParticipant)

    @Query("UPDATE participant SET uploaded=1 WHERE row_id=:primary")
    fun setUploaded(primary: Int)

    @Update
    fun updateParticipants(parts: List<DatabaseParticipant>)

    @Update
    fun updateParticipant(part: DatabaseParticipant)

    @Delete
    fun deleteParticipants(parts: List<DatabaseParticipant>)

    @Query("DELETE FROM participant")
    fun deleteAllParticipants()
}