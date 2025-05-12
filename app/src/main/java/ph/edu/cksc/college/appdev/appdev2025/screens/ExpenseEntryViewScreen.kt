package ph.edu.cksc.college.appdev.appdev2025.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import ph.edu.cksc.college.appdev.appdev2025.data.ExpenseEntry
import ph.edu.cksc.college.appdev.appdev2025.data.expenseCategoryList
import ph.edu.cksc.college.appdev.appdev2025.service.ExpenseService
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryViewScreen(
    expenseId: String,
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    val expenseService = remember { ExpenseService(auth, firestore) }
    var expense by remember { mutableStateOf<ExpenseEntry?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Load the expense entry
    LaunchedEffect(expenseId) {
        val doc = firestore.collection("expenseentries").document(expenseId).get().await()
        doc.data?.let { data ->
            expense = ExpenseEntry(
                id = doc.id,
                category = (data["category"] as? Long)?.toInt() ?: 0,
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                description = data["description"] as? String ?: "",
                dateTime = data["dateTime"] as? Timestamp ?: Timestamp.now(),
                userId = data["userId"] as? String ?: ""
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        expense?.let { exp ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Category: ${expenseCategoryList[exp.category].name}", style = MaterialTheme.typography.titleLarge)
                Text("Amount: ₱${String.format("%.2f", exp.amount)}", style = MaterialTheme.typography.titleLarge)
                Text("Description: ${exp.description}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Date: " + exp.dateTime.toDate().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showEditDialog && expense != null) {
        AddOrEditExpenseDialog(
            initialExpense = expense,
            onDismiss = { showEditDialog = false },
            onSave = { category, amount, description, dateTime ->
                val updated = expense!!.copy(
                    category = category,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    description = description,
                    dateTime = dateTime
                )
                coroutineScope.launch {
                    expenseService.updateExpense(updated)
                    expense = updated
                    showEditDialog = false
                    Toast.makeText(context, "Expense updated", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showDeleteDialog && expense != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        expenseService.deleteExpense(expense!!.id)
                        Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
} 