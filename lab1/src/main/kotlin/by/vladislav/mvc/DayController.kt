package by.vladislav.mvc

import javax.swing.JOptionPane

class DayController(private val model: DayModel) {

    lateinit var view: DayView
        internal set

    fun onEnterDataClicked() {
        val dialog = InputDialog(view.frame, model.input)
        dialog.isVisible = true

        if (!dialog.confirmed) return

        try {
            model.setData(dialog.wakeUpText, dialog.sleepText, dialog.workText, dialog.restText)
        } catch (e: IllegalArgumentException) {
            JOptionPane.showMessageDialog(
                view.frame,
                e.message ?: "Ошибка ввода.",
                "Ошибка ввода",
                JOptionPane.ERROR_MESSAGE,
            )
        }
    }
}
