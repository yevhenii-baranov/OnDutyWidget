package io.github.effffgen.onduty.service

import io.github.effffgen.onduty.Entity
import java.util.*
import java.util.concurrent.TimeUnit

val year = 2019
val month = Calendar.NOVEMBER
val day = 4

class OnDutyService {
    fun whoIsOnDutyToday(): Entity {
        val initialDate = Calendar.getInstance()
        initialDate.set(year, month, day, 0, 0, 0)

        val currentDate = Calendar.getInstance()

        val daysFromStart = TimeUnit.DAYS.convert(currentDate.timeInMillis - initialDate.timeInMillis, TimeUnit.MILLISECONDS)

        return when (daysFromStart % 3) {
            0L -> Entity.ICHI
            1L -> Entity.SHAP
            2L -> Entity.MARK
            else -> throw Exception("WTF?")
        }
    }
}