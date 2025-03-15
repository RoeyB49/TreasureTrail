package com.example.treasuretrail.data.repository

import android.util.Log
import com.example.treasuretrail.models.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getUserData(
        uid: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toUser() // We'll create an extension function below
                    onSuccess(user)
                } else {
                    // If the document doesn't exist, treat it as an error or handle it gracefully
                    onFailure(Exception("User document not found."))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}

// For convenience, an extension function to convert DocumentSnapshot -> User
private fun DocumentSnapshot.toUser(): User {
    Log.d("UserRepository", "Fetched document data: ${this.data}")
    return User(
        username = this.getString("username") ?: "",
        email = this.getString("email") ?: "",
        phoneNumber = this.getString("phoneNumber") ?: "",
        imageUri = this.getString("imageUri") ?: "",
        id = this.id
    )
}
