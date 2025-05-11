package ph.edu.cksc.college.appdev.appdev2025.screens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import ph.edu.cksc.college.appdev.appdev2025.data.Budget
import ph.edu.cksc.college.appdev.appdev2025.service.ExpenseService
import ph.edu.cksc.college.appdev.appdev2025.service.BudgetService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    firestore: FirebaseFirestore
) {
    val expenseService = ExpenseService(auth, firestore)
    val budgetService = BudgetService(auth, firestore)
    val expenses = remember { mutableStateListOf<ExpenseEntry>() }
    val coroutineScope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var budget by remember { mutableStateOf<Budget?>(null) }
    var totalSpent by remember { mutableStateOf(0.0) }
    var lastDeletedExpense by remember { mutableStateOf<ExpenseEntry?>(null) }

    LaunchedEffect(Unit) {
        expenseService.expenses.collect { expenseList ->
            expenses.clear()
            expenses.addAll(expenseList)
            totalSpent = expenseList.sumOf { it.amount }
        }
    }

    LaunchedEffect(Unit) {
        budgetService.budget.collect { budgetList ->
            budget = budgetList.firstOrNull()
        }
    }

    val remainingBudget = (budget?.amount ?: 0.0) - totalSpent
    val overBudget = remainingBudget < 0
    val progress = if ((budget?.amount ?: 0.0) > 0)
        (totalSpent / (budget?.amount ?: 1.0)).coerceIn(0.0, 1.0)
    else 0.0

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
                },
                actions = {
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Set Budget")
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
            // Budget section (clickable)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showBudgetDialog = true },
                colors = CardDefaults.cardColors(
                    containerColor = if (overBudget) Color(0xFFFFE0E0) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Budget: ", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "₱${String.format("%.2f", budget?.amount ?: 0.0)}",
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
                        progress = progress.toFloat(),
                        color = if (overBudget) Color.Red else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Remaining: ₱${String.format("%.2f", remainingBudget)}", color = if (overBudget) Color.Red else Color.Unspecified)
                }
            }
            // Summary section
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Spent: ₱${String.format("%.2f", totalSpent)}")
                Text("# Expenses: ${expenses.size}")
                Text("Avg: ₱${String.format("%.2f", if (expenses.isNotEmpty()) totalSpent/expenses.size else 0.0)}")
            }
            // Category breakdown
            LazyRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                items(expenseCategoryList) { category ->
                    val catTotal = expenses.filter { it.category == expenseCategoryList.indexOf(category) }.sumOf { it.amount }
                    if (catTotal > 0) {
                        Card(Modifier.padding(end = 8.dp), colors = CardDefaults.cardColors(containerColor = category.color.copy(alpha = 0.15f))) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(category.icon, contentDescription = null, tint = category.color)
                                Spacer(Modifier.width(4.dp))
                                Text("${category.name}: ₱${String.format("%.2f", catTotal)}", color = category.color)
                            }
                        }
                    }
                }
            }
            // Search and filter section
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search expenses...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            )

            // Category filter chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                }
                items(expenseCategoryList) { category ->
                    FilterChip(
                        selected = selectedCategory == expenseCategoryList.indexOf(category),
                        onClick = { selectedCategory = expenseCategoryList.indexOf(category) },
                        label = { Text(category.name) }
                    )
                }
            }

            // Expenses list
            if (expenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isNotEmpty()) Icons.Filled.Search else Icons.Filled.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) 
                                "No expenses found for '$searchQuery'" 
                            else 
                                "No expenses yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredExpenses = expenses.filter { expense ->
                        val matchesSearch = searchQuery.isEmpty() ||
                            expense.description.contains(searchQuery, ignoreCase = true) ||
                            expenseCategoryList[expense.category].name.contains(searchQuery, ignoreCase = true)
                        val matchesCategory = selectedCategory == null || expense.category == selectedCategory
                        matchesSearch && matchesCategory
                    }

                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onDelete = {
                                coroutineScope.launch {
                                    val id = expense.id
                                    if (id.isNotBlank()) {
                                        expenses.removeAll { it.id == id }
                                        budget = budget?.copy(amount = (budget?.amount ?: 0.0) + expense.amount)
                                        expenseService.deleteExpense(id)
                                    } else {
                                        expenses.remove(expense)
                                        budget = budget?.copy(amount = (budget?.amount ?: 0.0) + expense.amount)
                                    }
                                }
                            },
                            onEdit = { editedExpense ->
                                coroutineScope.launch {
                                    expenseService.updateExpense(editedExpense)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { category, amount, description ->
                coroutineScope.launch {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val newExpense = ExpenseEntry(
                        category = category,
                        amount = amt,
                        description = description
                    )
                    // Add to UI instantly
                    expenses.add(0, newExpense)
                    // Subtract from budget instantly
                    budget = budget?.copy(amount = (budget?.amount ?: 0.0) - amt)
                    expenseService.saveExpense(newExpense)
                    showAddDialog = false
                }
            }
        )
    }
    if (lastDeletedExpense != null) {
        // Add back to budget instantly
        budget = budget?.copy(amount = (budget?.amount ?: 0.0) + (lastDeletedExpense?.amount ?: 0.0))
        lastDeletedExpense = null
    }
    if (showBudgetDialog) {
        SetBudgetDialog(
            currentBudget = budget,
            onDismiss = { showBudgetDialog = false },
            onSet = { amount ->
                coroutineScope.launch {
                    val newBudget = Budget(
                        amount = amount,
                        period = "monthly", // for now
                        startDate = LocalDateTime.now().toString(),
                        endDate = "",
                        userId = auth.currentUser?.uid ?: ""
                    )
                    // Update UI instantly
                    if (budget == null) {
                        budget = newBudget
                        budgetService.saveBudget(newBudget)
                    } else {
                        budget = newBudget.copy(id = budget!!.id)
                        budgetService.updateBudget(newBudget.copy(id = budget!!.id))
                    }
                    showBudgetDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(
    expense: ExpenseEntry,
    onDelete: () -> Unit,
    onEdit: (ExpenseEntry) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₱${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    if (showEditDialog) {
        EditExpenseDialog(
            expense = expense,
            onDismiss = { showEditDialog = false },
            onEdit = { editedExpense ->
                onEdit(editedExpense)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (Int, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
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
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₱") }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (amount.isNotEmpty() && description.isNotEmpty()) {
                        onAdd(selectedCategory, amount, description)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    currentBudget: Budget?,
    onDismiss: () -> Unit,
    onSet: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(currentBudget?.amount?.toString() ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Budget Amount") },
                prefix = { Text("₱") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null) onSet(amt)
                }
            ) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: ExpenseEntry,
    onDismiss: () -> Unit,
    onEdit: (ExpenseEntry) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var description by remember { mutableStateOf(expense.description) }
    var expanded by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense") },
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
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₱") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onEdit(expense.copy(category = selectedCategory, amount = amt, description = description))
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
} 