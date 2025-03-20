package com.example.treasuretrail.data.repository

import android.net.Uri
import android.util.Log
import com.example.treasuretrail.models.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun updateUserProfile(
        userId: String,
        displayName: String? = null,
        phone: String? = null,
        photoUrl: Uri? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        val updates = mutableMapOf<String, Any>()
        displayName?.let { updates["displayName"] = it }
        phone?.let { updates["phone"] = it }

        if (photoUrl != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
            storageRef.putFile(photoUrl).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { downloadTask ->
                if (downloadTask.isSuccessful) {
                    updates["photoUrl"] = downloadTask.result.toString()
                    userRef.update(updates)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onFailure(e) }
                } else {
                    downloadTask.exception?.let { onFailure(it) }
                }
            }
        } else {
            if (updates.isNotEmpty()) {
                userRef.update(updates)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e) }
            } else {
                onSuccess()
            }
        }
    }


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
        displayName = this.getString("displayName") ?: "",
        email = this.getString("email") ?: "",
        phone = this.getString("phone") ?: "",
        photoUrl = this.getString("photoUrl") ?: "",
        id = this.id
    )
}
