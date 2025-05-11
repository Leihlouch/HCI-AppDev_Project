package ph.edu.cksc.college.appdev.appdev2025.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BudgetService(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection(BUDGET_COLLECTION)
    private val userId get() = auth.currentUser?.uid ?: ""

    suspend fun getBudget(): Double {
        val doc = collection.document(userId).get().await()
        return (doc.get("amount") as? Number)?.toDouble() ?: 0.0
    }

    suspend fun setBudget(amount: Double) {
        val data = mapOf(
            "amount" to amount,
            "userId" to userId
        )
        collection.document(userId).set(data).await()
    }

    companion object {
        private const val BUDGET_COLLECTION = "budgets"
    }
} 