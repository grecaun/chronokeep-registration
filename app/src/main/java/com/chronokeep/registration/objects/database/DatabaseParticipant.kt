package com.chronokeep.registration.objects.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Serializable
@Entity(tableName="participant", indices=[Index(value = ["first_name", "last_name", "distance", "birthdate", "chronokeep_info"], unique = true)])
class DatabaseParticipant (
    @Transient @PrimaryKey(autoGenerate = true) @ColumnInfo(name="row_id") val primary: Int = 0,
    @ColumnInfo(name="registration_id") val id: String,
    @ColumnInfo(name="bib") val bib: String,
    @ColumnInfo(name="first_name") val first: String,
    @ColumnInfo(name="last_name") val last: String,
    @ColumnInfo(name="birthdate") val birthdate: String,
    @ColumnInfo(name="gender") val gender: String,
    @ColumnInfo(name="distance") val distance: String,
    @ColumnInfo(name="mobile") val mobile: String,
    @ColumnInfo(name="sms_enabled") val sms: Boolean,
    @ColumnInfo(name="apparel") val apparel: String,
    @ColumnInfo(name="chronokeep_info", defaultValue = "") val chronokeep_info: String,
) {
    fun age(): String {
        return Period.between(
            LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("M/d/yyyy")),
            LocalDate.now()).years.toString()
    }

    fun toChronokeepParticipant(): ChronokeepParticipant {
        return ChronokeepParticipant(
            id = id,
            bib = bib,
            first = first,
            last = last,
            birthdate = birthdate,
            gender = gender,
            distance = distance,
            sms_enabled = sms,
            mobile = mobile,
            apparel = apparel,
            anonymous = false,
            age_group = ""
        )
    }
}