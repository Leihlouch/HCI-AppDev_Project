package ph.edu.cksc.college.appdev.appdev2025.screens

import androidx.compose.runtime.MutableState
import ph.edu.cksc.college.appdev.appdev2025.data.TaskEntry
import java.time.LocalDateTime

interface TaskEntryView {
    var taskEntry: MutableState<TaskEntry>
    var modified: Boolean

    fun onTitleChange(newValue: String)
    fun onDescriptionChange(newValue: String)
    fun onIsDoneChange(newValue: Boolean)
    fun onDateTimeChange(newValue: LocalDateTime)
    fun onDoneClick(popUpScreen: () -> Unit)
}