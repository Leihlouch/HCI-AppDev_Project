package ph.edu.cksc.college.appdev.appdev2025.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import ph.edu.cksc.college.appdev.appdev2025.data.ExpenseEntry

class ExpenseService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection(EXPENSE_COLLECTION)
        .whereEqualTo(USER_ID_FIELD, auth.currentUser?.uid)

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: Flow<List<ExpenseEntry>>
        get() = auth.currentUser?.let { user ->
            firestore
                .collection(EXPENSE_COLLECTION)
                .whereEqualTo(USER_ID_FIELD, user.uid)
                .orderBy(DATETIME_FIELD, Query.Direction.DESCENDING)
                .dataObjects()
        } ?: throw IllegalStateException("User must be logged in")

    suspend fun saveExpense(expense: ExpenseEntry): String {
        val updatedExpense = expense.copy(userId = auth.currentUser?.uid ?: "")
        val docRef = firestore.collection(EXPENSE_COLLECTION).add(updatedExpense).await()
        return docRef.id
    }

    suspend fun updateExpense(expense: ExpenseEntry) {
        val updatedExpense = expense.copy(userId = auth.currentUser?.uid ?: "")
        firestore.collection(EXPENSE_COLLECTION)
            .document(expense.id)
            .set(updatedExpense)
            .await()
    }

    suspend fun deleteExpense(expenseId: String) {
        firestore.collection(EXPENSE_COLLECTION).document(expenseId).delete().await()
    }

    suspend fun getExpense(expenseId: String): ExpenseEntry? =
        toExpenseObject(firestore.collection(EXPENSE_COLLECTION).document(expenseId).get().await())

    private fun toExpenseObject(document: DocumentSnapshot?): ExpenseEntry? {
        if (document == null)
            return null
        val data = document.data
        val date = data?.get("dateTime") as String
        val entry = ExpenseEntry(
            document.id,
            (data["category"] as Long).toInt(),
            (data["amount"] as Double),
            data["description"] as String,
            if (date.endsWith('Z')) date.substring(0, date.length - 1) else date,
            data["userId"] as String
        )
        Log.d("Expense", "${document.id} => ${document.data}")
        return entry
    }

    companion object {
        private const val USER_ID_FIELD = "userId"
        private const val DATETIME_FIELD = "dateTime"
        private const val EXPENSE_COLLECTION = "expenses"
    }
} 