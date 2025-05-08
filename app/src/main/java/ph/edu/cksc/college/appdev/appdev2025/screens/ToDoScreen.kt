package ph.edu.cksc.college.appdev.appdev2025.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import ph.edu.cksc.college.appdev.appdev2025.service.TaskStorageService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ph.edu.cksc.college.appdev.appdev2025.data.TaskEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    val context = LocalContext.current
    val taskService = remember { TaskStorageService(auth, firestore) }
    val firestoreTasks by taskService.getTasks().collectAsState(initial = emptyList())
    var tasks by remember { mutableStateOf(firestoreTasks) }
    val openDialog = remember { mutableStateOf(false) }
    val editingTask = remember { mutableStateOf<TaskEntry?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Keep local tasks in sync with Firestore
    LaunchedEffect(firestoreTasks) {
        tasks = firestoreTasks
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("To-Do List") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTask.value = null
                openDialog.value = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onEdit = {
                            editingTask.value = task
                            openDialog.value = true
                        },
                        onToggleDone = { isDone ->
                            coroutineScope.launch {
                                // Update in Firestore first
                                taskService.update(task.copy(isDone = isDone))
                                // Then update local state
                                tasks = tasks.map {
                                    if (it.id == task.id) it.copy(isDone = isDone) else it
                                }
                            }
                        },
                        onDelete = {
                            // Update UI instantly
                            tasks = tasks.filter { it.id != task.id }
                            // Then delete from Firestore in background
                            coroutineScope.launch {
                                try {
                                    taskService.delete(task.id)
                                } catch (e: Exception) {
                                    // If deletion fails, revert the UI change
                                    tasks = tasks + task
                                    Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    if (openDialog.value) {
        TaskDialog(
            initialTask = editingTask.value,
            onDismiss = { openDialog.value = false },
            onSave = { task ->
                coroutineScope.launch {
                    if (task.id.isEmpty()) {
                        taskService.save(task)
                        Toast.makeText(context, "Task added", Toast.LENGTH_SHORT).show()
                    } else {
                        taskService.update(task)
                        Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show()
                    }
                    openDialog.value = false
                }
            }
        )
    }
}

@Composable
fun TaskItem(
    task: TaskEntry,
    onEdit: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
    val date = try {
        LocalDateTime.parse(task.timestamp)
    } catch (e: Exception) {
        null
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggleDone(it) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .clickable { onEdit() }
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = date?.let { formatter.format(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    initialTask: TaskEntry?,
    onDismiss: () -> Unit,
    onSave: (TaskEntry) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var isDone by remember { mutableStateOf(initialTask?.isDone ?: false) }
    val timestamp = initialTask?.timestamp ?: LocalDateTime.now().toString()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTask == null) "Add Task" else "Edit Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { isDone = it }
                    )
                    Text("Done")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        TaskEntry(
                            id = initialTask?.id ?: "",
                            title = title,
                            description = description,
                            isDone = isDone,
                            timestamp = timestamp
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}