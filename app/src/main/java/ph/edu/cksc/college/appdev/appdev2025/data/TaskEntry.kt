package ph.edu.cksc.college.appdev.appdev2025.data

data class TaskEntry(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isDone: Boolean = false,
    val timestamp: String = ""
)