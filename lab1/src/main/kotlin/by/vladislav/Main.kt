package by.vladislav

import by.vladislav.mvc.DayController
import by.vladislav.mvc.DayModel
import by.vladislav.mvc.DayView
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        val model = DayModel()
        val controller = DayController(model)
        val view = DayView(model, controller)
        controller.view = view
    }
}
