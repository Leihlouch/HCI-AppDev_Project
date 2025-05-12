package ph.edu.cksc.college.appdev.appdev2025.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BudgetService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val budgetsCollection = firestore.collection("budgets")

    suspend fun getBudget(): Double {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        val doc = budgetsCollection.document(userId).get().await()
        return doc.getDouble("amount") ?: 0.0
    }

    suspend fun setBudget(amount: Double) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        budgetsCollection.document(userId).set(mapOf("amount" to amount)).await()
    }

    companion object {
        private const val BUDGET_COLLECTION = "budgets"
    }
} 