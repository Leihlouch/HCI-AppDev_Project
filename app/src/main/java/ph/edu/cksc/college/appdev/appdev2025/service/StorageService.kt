package ph.edu.cksc.college.appdev.appdev2025.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import ph.edu.cksc.college.appdev.appdev2025.data.DiaryEntry
import ph.edu.cksc.college.appdev.appdev2025.data.moodList

data class User(
    val id: String = "",
    val isAnonymous: Boolean = true
)

class StorageService(
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore
) {
    private val collection get() = firestore.collection(DIARYENTRY_COLLECTION)
        .whereEqualTo(USER_ID_FIELD, auth.currentUser?.uid)

    val currentUser: Flow<User>
        get() = callbackFlow {
            val listener =
                FirebaseAuth.AuthStateListener { auth ->
                    this.trySend(auth.currentUser?.let { User(it.uid, false) } ?: User())
                }
            auth.addAuthStateListener(listener)
            awaitClose { auth.removeAuthStateListener(listener) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val entries: Flow<List<DiaryEntry>>
        get() =
            currentUser.flatMapLatest { user ->
                firestore
                    .collection(DIARYENTRY_COLLECTION)
                    .whereEqualTo(USER_ID_FIELD, user.id)
                    .orderBy(DATETIME_FIELD, Query.Direction.DESCENDING)
                    .dataObjects()
            }

    // modified from last year because all of a sudden
    // the id is no longer retrieved, so mano-mano na lang
    fun getFilteredEntries(dateFilter: String): Flow<List<DiaryEntry>> {
        return flow {
            val snapshot = firestore
                .collection(DIARYENTRY_COLLECTION)
                .whereEqualTo(USER_ID_FIELD, auth.currentUser?.uid)
                .whereGreaterThanOrEqualTo(DATETIME_FIELD, dateFilter)
                .whereLessThanOrEqualTo(DATETIME_FIELD, "${dateFilter}\uf8ff")
                .orderBy(DATETIME_FIELD, Query.Direction.DESCENDING)
                .get().await()
            val list: MutableList<DiaryEntry> = ArrayList()
            for (document in snapshot) {
                val data = document.data
                val date = data["dateTime"] as String
                val entry = DiaryEntry(document.id,
                    (data["mood"] as Long).toInt(),
                    (data["star"] as Long).toInt(),
                    data["title"] as String,
                    data["themeSong"] as String,
                    data["content"] as String,
                    if (date.endsWith('Z')) date.substring(0, date.length - 1) else date,
                    data["userId"] as String)
                Log.d("Entry", "${document.id} => ${document.data}")
                list.add(entry)
            }
            emit(list.map { it })
        }
    }

    // Add new search function
    fun searchEntries(query: String): Flow<List<DiaryEntry>> {
        return flow {
            val snapshot = firestore
                .collection(DIARYENTRY_COLLECTION)
                .whereEqualTo(USER_ID_FIELD, auth.currentUser?.uid)
                .orderBy(DATETIME_FIELD, Query.Direction.DESCENDING)
                .get().await()
            
            val list: MutableList<DiaryEntry> = ArrayList()
            for (document in snapshot) {
                val data = document.data
                val date = data["dateTime"] as String
                val entry = DiaryEntry(document.id,
                    (data["mood"] as Long).toInt(),
                    (data["star"] as Long).toInt(),
                    data["title"] as String,
                    data["themeSong"] as String,
                    data["content"] as String,
                    if (date.endsWith('Z')) date.substring(0, date.length - 1) else date,
                    data["userId"] as String)
                
                // Filter entries based on search query
                if (query.isEmpty() ||
                    entry.title.contains(query, ignoreCase = true) ||
                    entry.content.contains(query, ignoreCase = true) ||
                    entry.themeSong.contains(query, ignoreCase = true) ||
                    moodList[entry.mood].mood.contains(query, ignoreCase = true)) {
                    list.add(entry)
                }
            }
            emit(list)
        }
    }

    suspend fun getDiaryEntry(diaryEntryId: String): DiaryEntry? =
        toObject(firestore.collection(DIARYENTRY_COLLECTION).document(diaryEntryId).get().await())

    private fun toObject(document: DocumentSnapshot?): DiaryEntry? {
        if (document == null)
            return null
        val data = document.data
        val date = data?.get("dateTime") as String
        val entry = DiaryEntry(document.id,
            (data["mood"] as Long).toInt(),
            (data["star"] as Long).toInt(),
            data["title"] as String,
            data["themeSong"] as String,
            data["content"] as String,
            if (date.endsWith('Z')) date.substring(0, date.length - 1) else date,
            data["userId"] as String)
        Log.d("Entry", "${document.id} => ${document.data}")
        return entry
    }

    suspend fun save(diaryEntry: DiaryEntry): String {
        val updatedDiaryEntry = diaryEntry.copy(userId = auth.currentUser?.uid ?: "")
        val docRef = firestore.collection(DIARYENTRY_COLLECTION).add(updatedDiaryEntry).await()
        return docRef.id
    }

    suspend fun update(diaryEntry: DiaryEntry) {
        val updatedDiaryEntry = diaryEntry.copy(userId = auth.currentUser?.uid ?: "")
        firestore.collection(DIARYENTRY_COLLECTION)
            .document(diaryEntry.id)
            .set(updatedDiaryEntry)
            .await()
    }

    suspend fun delete(diaryEntryId: String) {
        firestore.collection(DIARYENTRY_COLLECTION).document(diaryEntryId).delete().await()
    }

    suspend fun clearAll() {
        val userId = auth.currentUser?.uid ?: return
        val snapshot = firestore.collection(DIARYENTRY_COLLECTION)
            .whereEqualTo(USER_ID_FIELD, userId)
            .get().await()
        for (document in snapshot.documents) {
            firestore.collection(DIARYENTRY_COLLECTION).document(document.id).delete().await()
        }
    }

    companion object {
        private const val USER_ID_FIELD = "userId"
        private const val MOOD_FIELD = "mood"
        private const val TITLE_FIELD = "title"
        private const val THEME_SONG = "themeSong"
        private const val CONTENT_FIELD = "content"
        private const val DATETIME_FIELD = "dateTime"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val DIARYENTRY_COLLECTION = "entries"
        private const val SAVE_DIARYENTRY_TRACE = "saveDiaryEntry"
        private const val UPDATE_DIARYENTRY_TRACE = "updateDiaryEntry"
    }
}