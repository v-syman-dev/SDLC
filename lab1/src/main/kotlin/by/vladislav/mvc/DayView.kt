package by.vladislav.mvc

import java.awt.BorderLayout
import java.awt.Font
import java.util.Locale
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingConstants

class DayView(
    private val model: DayModel,
    private val controller: DayController,
) : DayModel.ModelListener {

    val frame = JFrame("Идеальный день")

    private val dataLabel = JLabel("Данные ещё не введены", SwingConstants.CENTER)
    private val resultArea = JTextArea()

    init {
        model.addListener(this)
        buildUi()
        updateResult()
    }

    private fun buildUi() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout()

        val header = JLabel("«Идеальный день»", SwingConstants.CENTER).apply {
            font = Font(font.name, Font.BOLD, 22)
        }

        dataLabel.font = Font(dataLabel.font.name, Font.PLAIN, 14)

        resultArea.isEditable = false
        resultArea.lineWrap = true
        resultArea.wrapStyleWord = true
        resultArea.font = Font(resultArea.font.name, Font.PLAIN, 14)
        resultArea.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val enterButton = JButton("Ввести данные").apply {
            addActionListener { controller.onEnterDataClicked() }
        }

        val center = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(12, 16, 12, 16)
            add(dataLabel, BorderLayout.NORTH)
            add(resultArea, BorderLayout.CENTER)
        }

        frame.add(header, BorderLayout.NORTH)
        frame.add(center, BorderLayout.CENTER)
        frame.add(enterButton, BorderLayout.SOUTH)

        frame.setSize(560, 360)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

    /** Обработчик уведомления модели: мгновенно обновляет интерфейс. */
    override fun onModelChanged() {
        updateResult()
    }

    private fun updateResult() {
        val input = model.input
        if (input == null) {
            dataLabel.text = "Данные ещё не введены"
            resultArea.text = "Нажмите «Ввести данные», чтобы начать."
            return
        }
        val result = model.result ?: return
        dataLabel.text = buildString {
            append("Подъём: ")
            append(input.wakeUp.format(DayModel.TIME_FORMATTER))
            append("  ·  Сон: ")
            append(formatHours(input.sleepHours))
            append(" ч  ·  Работа: ")
            append(formatHours(input.workHours))
            append(" ч  ·  Отдых: ")
            append(formatHours(input.restHours))
            append(" ч")
        }
        resultArea.text = buildString {
            appendLine("Оценка идеальности дня: ${result.score} / 100")
            appendLine()
            appendLine("Вердикт: ${result.verdict}")
            appendLine()
            appendLine("Альтернативное расписание «для счастья»:")
            appendLine("  • Сон: ${formatHours(result.recommendedSleep)} ч")
            appendLine("  • Работа: ${formatHours(result.recommendedWork)} ч")
            appendLine("  • Отдых: ${formatHours(result.recommendedRest)} ч")
        }
    }

    private fun formatHours(value: Double): String =
        String.format(Locale.ROOT, "%.1f", value).replace('.', ',')
}
