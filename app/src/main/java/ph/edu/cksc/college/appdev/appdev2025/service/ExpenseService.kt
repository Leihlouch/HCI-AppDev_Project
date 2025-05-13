package ph.edu.cksc.college.appdev.appdev2025.service

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import ph.edu.cksc.college.appdev.appdev2025.data.ExpenseEntry

class ExpenseService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val expensesCollection = firestore.collection("expenseentries")

    val expenses: Flow<List<ExpenseEntry>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val subscription = expensesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    try {
                        ExpenseEntry(
                            id = doc.id,
                            category = (data["category"] as? Long)?.toInt() ?: 0,
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            description = data["description"] as? String ?: "",
                            dateTime = data["dateTime"] as? Timestamp ?: Timestamp.now(),
                            userId = data["userId"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveExpense(expense: ExpenseEntry): String {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val expenseWithUserId = expense.copy(userId = userId)
        val data = mapOf(
            "category" to expenseWithUserId.category,
            "amount" to expenseWithUserId.amount,
            "description" to expenseWithUserId.description,
            "dateTime" to expenseWithUserId.dateTime,
            "userId" to expenseWithUserId.userId
        )
        val docRef = expensesCollection.add(data).await()
        return docRef.id
    }

    suspend fun updateExpense(expense: ExpenseEntry) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        if (expense.userId != userId) {
            throw IllegalArgumentException("Cannot update another user's expense")
        }
        val data = mapOf(
            "category" to expense.category,
            "amount" to expense.amount,
            "description" to expense.description,
            "dateTime" to expense.dateTime,
            "userId" to expense.userId
        )
        expensesCollection.document(expense.id).set(data).await()
    }

    suspend fun deleteExpense(expenseId: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val expense = expensesCollection.document(expenseId).get().await().data
            ?: throw IllegalArgumentException("Expense not found")
        if ((expense["userId"] as? String) != userId) {
            throw IllegalArgumentException("Cannot delete another user's expense")
        }
        expensesCollection.document(expenseId).delete().await()
    }

    suspend fun clearAll() {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val snapshot = expensesCollection.whereEqualTo("userId", userId).get().await()
        for (document in snapshot.documents) {
            expensesCollection.document(document.id).delete().await()
        }
    }
} 