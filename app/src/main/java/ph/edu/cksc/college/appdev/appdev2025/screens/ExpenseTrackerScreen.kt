package ph.edu.cksc.college.appdev.appdev2025.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import ph.edu.cksc.college.appdev.appdev2025.data.ExpenseEntry
import ph.edu.cksc.college.appdev.appdev2025.data.expenseCategoryList
import ph.edu.cksc.college.appdev.appdev2025.service.ExpenseService
import ph.edu.cksc.college.appdev.appdev2025.service.BudgetService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

@Composable
fun BudgetSection(
    budget: Double,
    totalSpent: Double
) {
    val remaining = budget - totalSpent
    val overBudget = remaining < 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (overBudget) Color(0xFFFFE0E0) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Budget: ", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "₱${String.format("%.2f", budget)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (overBudget) Color.Red else MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                if (overBudget) {
                    Icon(Icons.Filled.Warning, contentDescription = "Over budget", tint = Color.Red)
                    Text(" Over budget!", color = Color.Red)
                }
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = if (budget > 0) (totalSpent / budget).toFloat().coerceIn(0f, 1f) else 0f,
                color = if (overBudget) Color.Red else MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Remaining: ₱${String.format("%.2f", remaining)}",
                color = if (overBudget) Color.Red else Color.Unspecified
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    val expenseService = remember { ExpenseService(auth, firestore) }
    val budgetService = remember { BudgetService(auth, firestore) }
    var expenses by remember { mutableStateOf(listOf<ExpenseEntry>()) }
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<ExpenseEntry?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var budget by remember { mutableStateOf(0.0) }
    var totalSpent by remember { mutableStateOf(0.0) }
    var modified by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Fetch budget on launch
    LaunchedEffect(Unit) {
        budget = budgetService.getBudget()
    }

    // Collect expenses
    LaunchedEffect(Unit) {
        expenseService.expenses.collect { expenseList ->
            expenses = expenseList
            totalSpent = expenseList.sumOf { it.amount }
        }
    }

    // Debug: Print all expense IDs and descriptions to verify uniqueness
    LaunchedEffect(expenses) {
        expenses.forEach { Log.d("ExpenseDebug", "id=${it.id}, desc=${it.description}") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text("Expense Tracker") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Expense",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BudgetSection(budget = budget, totalSpent = totalSpent)
            Button(
                onClick = { showBudgetDialog = true },
                modifier = Modifier.align(Alignment.End).padding(end = 16.dp)
            ) {
                Text("Set Budget")
            }
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expenses yet", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses) { expense ->
                        ExpenseEntryCard(
                            expense = expense,
                            onEdit = { showEditDialog = expense },
                            onDelete = {
                                // Only try to delete from Firestore if id is valid
                                if (!expense.id.startsWith("pending-") && expense.id.isNotBlank()) {
                                    expenses = expenses.filter { it.id != expense.id }
                                    totalSpent -= expense.amount
                                    coroutineScope.launch {
                                        expenseService.deleteExpense(expense.id)
                                    }
                                } else {
                                    // Just remove from UI if temp or blank id
                                    expenses = expenses.filter { it.id != expense.id }
                                    totalSpent -= expense.amount
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOrEditExpenseDialog(
            onDismiss = { 
                if (modified) {
                    showDiscardDialog = true
                } else {
                    showAddDialog = false
                }
            },
            onSave = { category, amount, description, dateTime ->
                val tempId = "pending-" + System.currentTimeMillis()
                val newExpense = ExpenseEntry(
                    id = tempId, // Temporary ID for UI
                    category = category,
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    description = description,
                    dateTime = dateTime,
                    userId = auth.currentUser?.uid ?: ""
                )
                // Optimistically add to UI
                expenses = listOf(newExpense) + expenses
                totalSpent += newExpense.amount
                coroutineScope.launch {
                    val realId = expenseService.saveExpense(newExpense)
                    // Replace temp entry with real one (using Firestore doc ID)
                    expenses = expenses.map {
                        if (it.id == tempId) it.copy(id = realId) else it
                    }
                    totalSpent = expenses.sumOf { it.amount }
                }
                showAddDialog = false
                modified = false
            }
        )
    }

    if (showEditDialog != null) {
        AddOrEditExpenseDialog(
            initialExpense = showEditDialog,
            onDismiss = { 
                if (modified) {
                    showDiscardDialog = true
                } else {
                    showEditDialog = null
                }
            },
            onSave = { category, amount, description, dateTime ->
                val oldExpense = showEditDialog!!
                val newAmount = amount.toDoubleOrNull() ?: 0.0
                val updated = oldExpense.copy(
                    id = oldExpense.id, // Always keep the Firestore doc ID
                    category = category,
                    amount = newAmount,
                    description = description,
                    dateTime = dateTime,
                    userId = auth.currentUser?.uid ?: ""
                )
                // Optimistically update UI using the correct id
                expenses = expenses.map { 
                    if (it.id == oldExpense.id) updated else it 
                }
                totalSpent = expenses.sumOf { it.amount }
                // Only update in Firestore if id is valid
                if (!updated.id.startsWith("pending-") && updated.id.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            expenseService.updateExpense(updated)
                        } catch (e: Exception) {
                            // Revert changes if update fails
                            expenses = expenses.map { 
                                if (it.id == oldExpense.id) oldExpense else it 
                            }
                            totalSpent = expenses.sumOf { it.amount }
                        }
                    }
                }
                showEditDialog = null
                modified = false
            }
        )
    }

    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = budget,
            onDismiss = { showBudgetDialog = false },
            onSetBudget = { newBudget ->
                coroutineScope.launch {
                    budgetService.setBudget(newBudget)
                    budget = newBudget
                    showBudgetDialog = false
                }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes") },
            text = { Text("Are you sure you want to discard your changes?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        showAddDialog = false
                        showEditDialog = null
                        modified = false
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditExpenseDialog(
    initialExpense: ExpenseEntry? = null,
    onDismiss: () -> Unit,
    onSave: (Int, String, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialExpense?.category ?: 0) }
    var amount by remember { mutableStateOf(if (initialExpense != null) initialExpense.amount.toString() else "") }
    var description by remember { mutableStateOf(initialExpense?.description ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var dateTime by remember { mutableStateOf(initialExpense?.dateTime ?: LocalDateTime.now().toString()) }
    var modified by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialExpense == null) "Add Expense" else "Edit Expense") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = expenseCategoryList[selectedCategory].name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        expenseCategoryList.forEachIndexed { index, category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = index
                                    expanded = false
                                    modified = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = category.icon,
                                        contentDescription = null,
                                        tint = category.color
                                    )
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        modified = true
                    },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₱") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        modified = true
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (amount.isNotEmpty() && description.isNotEmpty()) {
                        onSave(selectedCategory, amount, description, dateTime)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExpenseEntryCard(
    expense: ExpenseEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = expenseCategoryList[expense.category].icon,
                    contentDescription = null,
                    tint = expenseCategoryList[expense.category].color
                )
                Column {
                    Text(
                        text = expenseCategoryList[expense.category].name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = LocalDateTime.parse(expense.dateTime)
                            .format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₱${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun SetBudgetDialog(currentBudget: Double, onDismiss: () -> Unit, onSetBudget: (Double) -> Unit) {
    var budgetInput by remember { mutableStateOf(currentBudget.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget") },
        text = {
            OutlinedTextField(
                value = budgetInput,
                onValueChange = { budgetInput = it },
                label = { Text("Budget Amount") },
                prefix = { Text("₱") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = budgetInput.toDoubleOrNull()
                    if (value != null) {
                        onSetBudget(value)
                    }
                }
            ) {
                Text("Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 