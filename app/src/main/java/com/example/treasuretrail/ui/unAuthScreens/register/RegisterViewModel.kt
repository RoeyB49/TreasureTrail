package com.example.treasuretrail.ui.unAuthScreens.register

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> = _registrationSuccess

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        // Create user record in Firestore with available Google info
                        val userMap = hashMapOf(
                            "uid" to user.uid,
                            "email" to user.email,
                            "displayName" to user.displayName,
                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                            "registrationType" to "google"
                        )
                        firestore.collection("users").document(user.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                _registrationSuccess.value = true
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                _registrationSuccess.value = false
                                onFailure(e.message ?: "Failed to save user data")
                            }
                    } else {
                        _registrationSuccess.value = false
                        onFailure("User is null after authentication")
                    }
                } else {
                    _registrationSuccess.value = false
                    onFailure(task.exception?.message ?: "Authentication failed")
                }
            }
    }

    fun registerUser(
        userName: String,
        email: String,
        password: String,
        phone: String,
        profileImageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Create user account with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        if (profileImageUri != null) {
                            uploadProfileImage(user.uid, profileImageUri) { imageUrl ->
                                // Update profile with display name and photo
                                updateUserProfile(user.uid, userName, phone, imageUrl, onSuccess, onFailure)
                            }
                        } else {
                            // No profile image, just update user data
                            updateUserProfile(user.uid, userName, phone, null, onSuccess, onFailure)
                        }
                    } else {
                        _registrationSuccess.value = false
                        onFailure("User is null after registration")
                    }
                } else {
                    _registrationSuccess.value = false
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }

    private fun uploadProfileImage(userId: String, imageUri: Uri, onComplete: (String) -> Unit) {
        val filename = "profile_${userId}_${UUID.randomUUID()}"
        val storageRef = storage.reference.child("profile_images/$filename")

        storageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result.toString()
                    onComplete(downloadUri)
                } else {
                    // If image upload fails, continue without image
                    onComplete("")
                }
            }
    }

    private fun updateUserProfile(
        userId: String,
        userName: String,
        phone: String,
        imageUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser

        // Update display name and photo URL if available
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .apply {
                imageUrl?.let { if (it.isNotEmpty()) setPhotoUri(Uri.parse(it)) }
            }
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    // Save additional user data to Firestore
                    val userMap = hashMapOf(
                        "uid" to userId,
                        "displayName" to userName,
                        "email" to user.email,
                        "phone" to phone,
                        "photoUrl" to (imageUrl ?: ""),
                        "registrationType" to "email"
                    )

                    firestore.collection("users").document(userId)
                        .set(userMap)
                        .addOnSuccessListener {
                            _registrationSuccess.value = true
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            _registrationSuccess.value = false
                            onFailure(e.message ?: "Failed to save user data")
                        }
                } else {
                    _registrationSuccess.value = false
                    onFailure(profileTask.exception?.message ?: "Failed to update profile")
                }
            }
    }
}