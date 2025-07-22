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

    @Query("SELECT * FROM participant WHERE registration_id=:id")
    fun getParticipantById(id: String): List<DatabaseParticipant>

    @Query("SELECT * FROM participant WHERE first_name=:first " +
            "AND last_name=:last " +
            "AND birthdate=:birthdate " +
            "AND gender=:gender " +
            "AND distance=:distance " +
            "AND chronokeep_info=:info")
    fun getParticipant(first: String, last: String, birthdate: String, gender: String, distance: String, info: String): List<DatabaseParticipant>

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