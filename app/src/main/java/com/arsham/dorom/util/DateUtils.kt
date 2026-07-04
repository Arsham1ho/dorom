package com.arsham.dorom.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun todayString(): String = LocalDate.now().toString() // yyyy-MM-dd

fun mondayOfWeek(date: LocalDate = LocalDate.now()): LocalDate =
    date.minusDays((date.dayOfWeek.value - 1).toLong())

fun LocalDate.pretty(): String = format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))

fun dayOfWeekLabel(dayOfWeek: Int): String =
    DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

fun yearMonthString(date: LocalDate = LocalDate.now()): String =
    date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
