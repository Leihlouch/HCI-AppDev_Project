package ph.edu.cksc.college.appdev.appdev2025.screens

import androidx.compose.runtime.MutableState
import ph.edu.cksc.college.appdev.appdev2025.data.DiaryEntry
import java.time.LocalDateTime

interface DiaryEntryView {

    var diaryEntry: MutableState<DiaryEntry>

    var modified: Boolean

    fun onTitleChange(newValue: String)

    fun onContentChange(newValue: String)

    fun onMoodChange(newValue: Int)

    fun onStarChange(newValue: Int)

    fun onDateTimeChange(newValue: LocalDateTime)

    fun onThemeSongChange(newValue: String)

    fun onDoneClick(popUpScreen: () -> Unit)
}