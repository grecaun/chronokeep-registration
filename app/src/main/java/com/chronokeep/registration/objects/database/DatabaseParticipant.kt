package com.chronokeep.registration.objects.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Serializable
@Entity(tableName="participant", indices=[Index(value = ["first_name", "last_name", "distance", "birthdate", "chronokeep_info"], unique = true)])
class DatabaseParticipant (
    @PrimaryKey @ColumnInfo(name="registration_id") val id: String,
    @ColumnInfo(name="bib") val bib: String,
    @ColumnInfo(name="first_name") val first: String,
    @ColumnInfo(name="last_name") val last: String,
    @ColumnInfo(name="birthdate") val birthdate: String,
    @ColumnInfo(name="gender") val gender: String,
    @ColumnInfo(name="distance") val distance: String,
    @ColumnInfo(name="mobile") val mobile: String,
    @ColumnInfo(name="sms_enabled") val sms: Boolean,
    @ColumnInfo(name="apparel") val apparel: String,
    @ColumnInfo(name="updated_at", defaultValue = "0") val updatedAt: Long,
    @ColumnInfo(name="chronokeep_info", defaultValue = "") val chronokeepInfo: String,
    @ColumnInfo(name="uploaded", defaultValue = "false") var uploaded: Boolean,
) {
    fun age(): String {
        return try {
            Period.between(
                LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("M/d/yyyy")),
                LocalDate.now()).years.toString()
        } catch (_: Exception) {
            ""
        }
    }

    fun matches(other: DatabaseParticipant): Boolean {
        return this.first.equals(other.first, true) && this.last.equals(other.last, true)
                && this.bib.equals(other.bib, true) && this.birthdate.equals(other.birthdate, true)
                && this.gender.equals(other.gender, true) && this.distance.equals(other.distance, true)
                && this.mobile.equals(other.mobile, true) && this.sms == other.sms
                && this.apparel.equals(other.apparel, true) && this.chronokeepInfo == other.chronokeepInfo
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
            age_group = "",
            updated_at = updatedAt
        )
    }
}