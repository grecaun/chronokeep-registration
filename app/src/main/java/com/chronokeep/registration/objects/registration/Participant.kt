package com.chronokeep.registration.objects.registration

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@Serializable
class Participant (
    val id: Int,
    val bib: String,
    val first: String,
    val last: String,
    val birthdate: String,
    val gender: String,
    val distance: String,
    val mobile: String,
    val sms: Boolean,
    val apparel: String,
) {
    fun age(): String {
        return Period.between(
            LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("M/d/yyyy")),
            LocalDate.now()).years.toString()
    }

    fun isUpdated(other: Participant): Boolean {
        return (bib != other.bib || first != other.first
                || last != other.last || birthdate != other.birthdate
                || gender != other.gender || distance != other.distance
                || mobile != other.mobile || sms != other.sms
                || apparel != other.apparel)
    }
}