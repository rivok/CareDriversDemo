package com.hopskipdrive.caredriversdemo.utils.extensions.kotlinx.datetime

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

private val localDateFormatter by lazy {
    DateTimeFormatter.ofPattern("EE M/d")
}

private val localTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("h:mma")
}

val Instant.localDate: LocalDate
    get() = localDateTime.date

val Instant.localDateTime: LocalDateTime
    get() = toLocalDateTime(TimeZone.currentSystemDefault())

fun Instant.formatLocalDate(): String =
    localDateFormatter.format(localDate.toJavaLocalDate())

fun Instant.formatLocalTime(): String =
    localTimeFormatter.format(localDateTime.toJavaLocalDateTime()).lowercase().let { string ->
        // hack - shave off the "m" and lowercase it
        string.substring(startIndex = 0, endIndex = string.length - 1).lowercase()
    }
