package com.chronokeep.registration.network.chronokeep.objects

import com.chronokeep.registration.objects.database.DatabaseParticipant
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("unused", "PropertyName")
@Serializable
class ChronokeepParticipant (
    val id: String,
    val bib: String,
    val first: String,
    val last: String,
    val birthdate: String,
    val gender: String,
    val age_group: String,
    val distance: String,
    val anonymous: Boolean,
    val sms_enabled: Boolean,
    val mobile: String,
    val apparel: String,
    val updated_at: Long,
) {
    fun toDatabaseParticipant(chronokeepInfo: String): DatabaseParticipant {
        val date = try {
            LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("M/d/yyyy"))
        } catch (_: Exception) {
            LocalDate.now()
        }
        val day = date.dayOfMonth
        val month = date.monthValue
        val year = date.year
        return DatabaseParticipant(
            id = id,
            bib = bib,
            first = first,
            last = last,
            birthdate = "$month/$day/$year",
            gender = gender,
            distance = distance,
            sms = sms_enabled,
            mobile = mobile,
            apparel = apparel,
            chronokeep_info = chronokeepInfo,
            uploaded = true,
            updated_at = updated_at
        )
    }
}