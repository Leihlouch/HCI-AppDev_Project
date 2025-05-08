package ph.edu.cksc.college.appdev.appdev2025.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import ph.edu.cksc.college.appdev.appdev2025.data.TaskEntry

// ... existing code ...
class TaskStorageService(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore
) {
    fun getTasks(): Flow<List<TaskEntry>> = flow {
        val snapshot = firestore
            .collection(TASKENTRY_COLLECTION)
            .orderBy(TIMESTAMP_FIELD, Query.Direction.DESCENDING)
            .get().await()
        val list: MutableList<TaskEntry> = ArrayList()
        for (document in snapshot) {
            val data = document.data
            val entry = TaskEntry(
                id = document.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                isDone = data["isDone"] as? Boolean ?: false,
                timestamp = data["timestamp"] as? String ?: ""
            )
            list.add(entry)
        }
        emit(list)
    }

    suspend fun getTaskEntry(taskEntryId: String): TaskEntry? =
        toObject(firestore.collection(TASKENTRY_COLLECTION).document(taskEntryId).get().await())

    private fun toObject(document: DocumentSnapshot?): TaskEntry? {
        if (document == null) return null
        val data = document.data
        return TaskEntry(
            id = document.id,
            title = data?.get("title") as? String ?: "",
            description = data?.get("description") as? String ?: "",
            isDone = data?.get("isDone") as? Boolean ?: false,
            timestamp = data?.get("timestamp") as? String ?: ""
        )
    }

    suspend fun save(taskEntry: TaskEntry): String {
        firestore.collection(TASKENTRY_COLLECTION).add(taskEntry)
        return ""
    }

    suspend fun update(taskEntry: TaskEntry) {
        firestore.collection(TASKENTRY_COLLECTION).document(taskEntry.id).set(taskEntry)
    }

    suspend fun delete(taskEntryId: String) {
        firestore.collection(TASKENTRY_COLLECTION).document(taskEntryId).delete().await()
    }

    companion object {
        private const val TIMESTAMP_FIELD = "timestamp"
        private const val TASKENTRY_COLLECTION = "taskentries"
    }
}
// ... existing code ...