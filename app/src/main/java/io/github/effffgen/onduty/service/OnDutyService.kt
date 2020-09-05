package io.github.effffgen.onduty.service

import io.github.effffgen.onduty.Entity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

val initialDate: LocalDate = LocalDate.of(2019, 11, 4)

class OnDutyService {
    fun whoIsOnDutyToday(): Entity {
        val currentDate = LocalDate.now()
        val daysFromStart = ChronoUnit.DAYS.between(initialDate, currentDate)

        println(daysFromStart)
        println(daysFromStart % 3)

        return when (daysFromStart % 3) {
            0L -> Entity.ICHI
            1L -> Entity.SHAP
            2L -> Entity.MARK
            else -> throw Exception("WTF?")
        }
    }
}