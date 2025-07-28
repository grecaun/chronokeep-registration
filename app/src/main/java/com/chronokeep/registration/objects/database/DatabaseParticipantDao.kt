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
    fun addParticipantsInternal(parts: List<DatabaseParticipant>)

    fun addParticipants(parts: List<DatabaseParticipant>) {
        // Check participants in order to ensure we don't override known bib numbers entered.
        val toAdd = ArrayList<DatabaseParticipant>()
        for (p in parts) {
            var oldPart = getParticipantById(p.id)
            // If participant can't be found by ID, check for a match by other fields
            if (oldPart.isEmpty()) {
                oldPart = getParticipant(p.first, p.last, p.birthdate, p.gender, p.distance, p.chronokeep_info)
            }
            // If none found, or no bib set, add to list to add to database.
            if (oldPart.isEmpty() || (oldPart.size == 1 && oldPart[0].bib.isBlank())) {
                toAdd.add(p)
            }
        }
        addParticipantsInternal(toAdd)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addParticipantInternal(part: DatabaseParticipant)

    fun addParticipant(part: DatabaseParticipant) {
        var oldPart = getParticipantById(part.id)
        // If participant can't be found by ID, check for a match by other fields
        if (oldPart.isEmpty()) {
            oldPart = getParticipant(part.first, part.last, part.birthdate, part.gender, part.distance, part.chronokeep_info)
        }
        // If none found, or no bib set, add to list to add to database.
        if (oldPart.isEmpty() || (oldPart.size == 1 && oldPart[0].bib.isBlank())) {
            addParticipantInternal(part)
        }
    }

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