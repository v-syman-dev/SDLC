package by.vladislav.mvc

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

class DayModel {
    fun interface ModelListener {
        fun onModelChanged()
    }

    private val listeners = mutableListOf<ModelListener>()

    data class DayInput(
        val wakeUp: LocalTime,
        val sleepHours: Double,
        val workHours: Double,
        val restHours: Double,
    )

    data class DayResult(
        val score: Int,
        val verdict: String,
        val recommendedSleep: Double,
        val recommendedWork: Double,
        val recommendedRest: Double,
    )

    companion object {
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
        val IDEAL_WAKE_UP: LocalTime = LocalTime.of(7, 0)
        const val IDEAL_SLEEP = 8.0
        const val IDEAL_WORK = 8.0
        const val IDEAL_REST = 3.0
        const val HOURS_IN_DAY = 24.0
        const val MAX_FIELD_HOURS = 24.0
        const val DAY_BUFFER = 2.0 // резерв на быт/дорогу
    }

    var input: DayInput? = null
        private set
    var result: DayResult? = null
        private set

    fun addListener(listener: ModelListener) = listeners.add(listener)
    fun removeListener(listener: ModelListener) = listeners.remove(listener)

    fun setData(wakeUpText: String, sleepText: String, workText: String, restText: String) {
        input = parse(wakeUpText, sleepText, workText, restText)
        result = calculate(input!!)
        listeners.forEach { it.onModelChanged() }
    }

    fun parse(wakeUpText: String, sleepText: String, workText: String, restText: String): DayInput {
        val wakeUp = parseTime(wakeUpText)
        val sleep = parseHours(sleepText, "сон")
        val work = parseHours(workText, "работа")
        val rest = parseHours(restText, "отдых")
        if (sleep + work + rest > HOURS_IN_DAY) {
            throw IllegalArgumentException("Сумма сна, работы и отдыха не может превышать 24 часа в сутки!")
        }
        return DayInput(wakeUp, sleep, work, rest)
    }

    fun calculate(input: DayInput): DayResult {
        val score = computeScore(input)
        val rec = computeRecommendedSchedule(input)
        return DayResult(
            score = score,
            verdict = computeVerdict(score, input),
            recommendedSleep = rec[0],
            recommendedWork = rec[1],
            recommendedRest = rec[2],
        )
    }

    /** Оценка 0..100: сон 35, работа 25, отдых 25, время подъёма 15. */
    private fun computeScore(input: DayInput): Int {
        val sleep = componentScore(input.sleepHours, IDEAL_SLEEP, max = 35.0, tolerance = 0.5, penalty = 8.0)
        val work = componentScore(input.workHours, IDEAL_WORK, max = 25.0, tolerance = 1.0, penalty = 4.0)
        val rest = componentScore(input.restHours, IDEAL_REST, max = 25.0, tolerance = 1.0, penalty = 5.0)
        val wake = componentScore(wakeUpDiffHours(input.wakeUp), 0.0, max = 15.0, tolerance = 0.5, penalty = 4.0)
        return (sleep + work + rest + wake).roundToInt().coerceIn(0, 100)
    }

    /** В пределах допуска — max, дальше линейно вниз. */
    private fun componentScore(value: Double, ideal: Double, max: Double, tolerance: Double, penalty: Double): Double {
        val deviation = abs(value - ideal)
        if (deviation <= tolerance) return max
        return (max - (deviation - tolerance) * penalty).coerceAtLeast(0.0)
    }

    private fun wakeUpDiffHours(wakeUp: LocalTime): Double =
        abs(wakeUp.toSecondOfDay() - IDEAL_WAKE_UP.toSecondOfDay()) / 3600.0

    private fun computeVerdict(score: Int, input: DayInput): String {
        val base = when {
            score >= 85 -> "Идеальный день! Отличный баланс сна, работы и отдыха."
            score >= 70 -> "Хороший день, но есть куда расти."
            score >= 50 -> "Средний день. Стоит пересмотреть расписание."
            score >= 30 -> "Слабый день, режим разбалансирован."
            else -> "День требует серьёзной правки. Обрати внимание на рекомендации."
        }
        val hint = when {
            input.sleepHours < IDEAL_SLEEP - 0.5 -> " Тебе не хватает сна."
            input.workHours > IDEAL_WORK + 1 -> " Многовато работы — риск выгорания."
            input.restHours < IDEAL_REST - 1 -> " Мало отдыха."
            wakeUpDiffHours(input.wakeUp) > 1.5 -> " Стоит скорректировать время подъёма."
            else -> ""
        }
        return base + hint
    }

    /** Адаптивное расписание: тянем значения на 60% к идеалу и укладываем в сутки. */
    private fun computeRecommendedSchedule(input: DayInput): DoubleArray {
        val sleep = round1(moveToward(input.sleepHours, IDEAL_SLEEP).coerceIn(6.0, 10.0))
        val work = round1(moveToward(input.workHours, IDEAL_WORK).coerceIn(0.0, IDEAL_WORK))
        var rest = moveToward(input.restHours, IDEAL_REST).coerceAtLeast(IDEAL_REST)
        val overflow = sleep + work + rest - (HOURS_IN_DAY - DAY_BUFFER)
        if (overflow > 0) rest = (rest - overflow).coerceAtLeast(1.0)
        return doubleArrayOf(sleep, work, round1(rest))
    }

    private fun moveToward(current: Double, target: Double): Double = current + (target - current) * 0.6
    private fun round1(v: Double): Double = (v * 10).roundToInt() / 10.0

    private fun parseTime(text: String): LocalTime {
        val parts = text.trim().split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Время подъёма введите в формате ЧЧ:ММ, например 7:30!")
        }
        val hour = parts[0].trim().toIntOrNull()
            ?: throw IllegalArgumentException("Некорректный час подъёма!")
        val minute = parts[1].trim().toIntOrNull()
            ?: throw IllegalArgumentException("Некорректные минуты подъёма!")
        if (hour !in 0..23) throw IllegalArgumentException("Час подъёма должен быть от 0 до 23!")
        if (minute !in 0..59) throw IllegalArgumentException("Минуты подъёма должны быть от 0 до 59!")
        return LocalTime.of(hour, minute)
    }

    private fun parseHours(text: String, fieldName: String): Double {
        val value = text.trim().replace(',', '.').toDoubleOrNull()
            ?: throw IllegalArgumentException("Некорректное значение поля «$fieldName»!")
        if (value.isNaN() || value.isInfinite())
            throw IllegalArgumentException("Поле «$fieldName» должно быть конечным числом!")
        if (value < 0) throw IllegalArgumentException("Поле «$fieldName» не может быть отрицательным!")
        if (value > MAX_FIELD_HOURS) throw IllegalArgumentException("Поле «$fieldName» не может превышать 24 часа!")
        return value
    }
}
