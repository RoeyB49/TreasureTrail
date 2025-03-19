package com.example.treasuretrail.data

import AppDatabase
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.treasuretrail.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(context: Context) {
    private val userDao = AppDatabase.getDatabase(context).userDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUser(id: String): LiveData<User> {
        return userDao.getUser(id)
    }

    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insertUser(user)
        }
    }


    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.updateUser(user)  // Update in local storage
            firestore.collection("users").document(user.id).set(user) // Update in Firebase
        }
    }

    fun updateUserProfile(
        userId: String,
        username: String? = null,
        phoneNumber: String? = null,
        imageUri: Uri? = null,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        val updates = mutableMapOf<String, Any>()
        username?.let { updates["username"] = it }
        phoneNumber?.let { updates["phoneNumber"] = it }

        if (imageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
            storageRef.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.addOnCompleteListener { downloadTask ->
                if (downloadTask.isSuccessful) {
                    updates["imageUri"] = downloadTask.result.toString()
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

}
