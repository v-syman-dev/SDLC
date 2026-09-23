package by.vladislav.mvc

import java.awt.Frame
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.util.Locale
import javax.swing.AbstractAction
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.KeyStroke

class InputDialog(owner: Frame, values: DayModel.DayInput?) : JDialog(owner, "Ввод данных", true) {

    private val wakeUpField = JTextField(8)
    private val sleepField = JTextField(8)
    private val workField = JTextField(8)
    private val restField = JTextField(8)

    /** Флаг подтверждения: true, если пользователь нажал «ОК». */
    var confirmed: Boolean = false
        private set

    init {
        setValues(values)
        buildUi()
    }

    fun setValues(values: DayModel.DayInput?) {
        if (values == null) return
        wakeUpField.text = values.wakeUp.format(DayModel.TIME_FORMATTER)
        sleepField.text = formatHours(values.sleepHours)
        workField.text = formatHours(values.workHours)
        restField.text = formatHours(values.restHours)
    }

    val wakeUpText: String get() = wakeUpField.text
    val sleepText: String get() = sleepField.text
    val workText: String get() = workField.text
    val restText: String get() = restField.text

    private fun buildUi() {
        isResizable = false
        contentPane = createContentPanel()

        val confirmAction = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                confirmed = true
                dispose()
            }
        }
        val cancelAction = object : AbstractAction() {
            override fun actionPerformed(e: ActionEvent) {
                confirmed = false
                dispose()
            }
        }
        listOf(wakeUpField, sleepField, workField, restField).forEach { field ->
            field.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "confirm")
            field.actionMap.put("confirm", confirmAction)
            field.inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel")
            field.actionMap.put("cancel", cancelAction)
        }

        pack()
        setLocationRelativeTo(owner)
        defaultCloseOperation = DISPOSE_ON_CLOSE
    }

    private fun createContentPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        panel.border = BorderFactory.createEmptyBorder(16, 16, 16, 16)
        val gbc = GridBagConstraints().apply {
            insets = Insets(6, 6, 6, 6)
            anchor = GridBagConstraints.WEST
        }

        addLabeledField(panel, gbc, 0, "Время подъёма (ЧЧ:ММ):", wakeUpField)
        addLabeledField(panel, gbc, 1, "Сон (часов):", sleepField)
        addLabeledField(panel, gbc, 2, "Работа (часов):", workField)
        addLabeledField(panel, gbc, 3, "Отдых (часов):", restField)

        val okButton = JButton("OK")
        okButton.addActionListener {
            confirmed = true
            dispose()
        }
        val cancelButton = JButton("Отмена")
        cancelButton.addActionListener {
            confirmed = false
            dispose()
        }

        gbc.gridx = 1
        gbc.gridy = 4
        gbc.anchor = GridBagConstraints.EAST
        gbc.insets = Insets(12, 6, 6, 6)
        panel.add(okButton, gbc)
        gbc.gridx = 2
        panel.add(cancelButton, gbc)

        rootPane.defaultButton = okButton
        return panel
    }

    private fun addLabeledField(panel: JPanel, gbc: GridBagConstraints, row: Int, label: String, field: JTextField) {
        gbc.gridx = 0
        gbc.gridy = row
        gbc.gridwidth = 1
        gbc.fill = GridBagConstraints.NONE
        panel.add(JLabel(label), gbc)

        gbc.gridx = 1
        gbc.gridwidth = 2
        gbc.fill = GridBagConstraints.HORIZONTAL
        panel.add(field, gbc)
    }

    private fun formatHours(value: Double): String =
        String.format(Locale.ROOT, "%.1f", value).replace('.', ',')
}
