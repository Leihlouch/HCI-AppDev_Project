package ph.edu.cksc.college.appdev.appdev2025.service

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ph.edu.cksc.college.appdev.appdev2025.data.UserData

data class UserPass (
    val email: String,
    val password: String
)

class AuthService(
    private val context: Context,
    val auth: FirebaseAuth,
    val firestore: FirebaseFirestore
) {
    val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    suspend fun saveUser(email: String, password: String) {
        val editor = sharedPref.edit()
        editor.putString("email", email)
        editor.putString("password", password)
        editor.apply()
    }

    fun readUser(): UserPass {
        return UserPass(sharedPref.getString("email", "") ?: "", sharedPref.getString("password", "") ?: "")
    }

    fun loginUser(email: String, password: String,
                  onSuccess: (message: String) -> Unit,
                  onFailure: (message: String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(auth.currentUser?.email + " is successfully logged")
                } else {
                    onFailure("Login failed: ${task.exception}")
                }
            }
    }

    fun logoutUser() {
        auth.signOut()
        val editor = sharedPref.edit()
        editor.putString("email", "")
        editor.putString("password", "")
        editor.apply()
    }

    fun registerUser(email: String, password: String, username: String,
                     onSuccess: (message: String) -> Unit,
                     onFailure: (message: String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(auth.currentUser?.email + " is successfully registered and logged")
                    saveUserData(auth.currentUser?.uid ?: "", email, username)
                } else {
                    onFailure("Registration failed: ${task.exception}")
                }
            }
    }

    private fun saveUserData(userId: String, email: String, username: String) {
        val userData = UserData(userId, email, username)
        val userRef = firestore.collection("users").document(userId)

        userRef.set(userData)
            .addOnSuccessListener {
                Log.d("saveUserData", "Success")
            }
            .addOnFailureListener {
                Log.d("saveUserData", "Failed")
            }
    }
}