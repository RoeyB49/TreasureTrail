package com.example.treasuretrail.ui.unAuthScreens.register

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.treasuretrail.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    /**
     * Regular registration using email and password.
     * Optionally uploads a profile image.
     */
    fun registerUser(userName: String, email: String, password: String, phone: String, imageUri: Uri?) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid
                        if (userId != null) {
                            if (imageUri != null) {
                                uploadProfileImage(userId, imageUri) { imageUrl ->
                                    saveUserToFirestore(userId, userName, email, phone, imageUrl)
                                }
                            } else {
                                saveUserToFirestore(userId, userName, email, phone, null)
                            }
                        }
                    } else {
                        val error = task.exception
                        val errorMsg = when {
                            error?.message?.contains("already in use") == true ->
                                "The email address is already in use. Please use a different email."
                            else -> "Registration failed. Please try again."
                        }
                        _errorMessage.postValue(errorMsg)
                        _registrationSuccess.postValue(false)
                    }
                }
        }
    }

    /**
     * Saves the newly created user to Firestore.
     */
    private fun saveUserToFirestore(userId: String, userName: String, email: String, phone: String, imageUri: String?) {
        val newUser = User(
            id = userId,
            username = userName,
            email = email,
            password = "", // Ensure you handle passwords securely!
            phoneNumber = phone,
            imageUri = imageUri
        )
        firestore.collection("users").document(userId)
            .set(newUser)
            .addOnSuccessListener {
                _registrationSuccess.postValue(true)
            }
            .addOnFailureListener {
                _registrationSuccess.postValue(false)
            }
    }

    /**
     * Uploads the profile image to Firebase Storage and returns its URL.
     */
    private fun uploadProfileImage(userId: String, imageUri: Uri, onComplete: (String?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/$userId.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    /**
     * Signs in using a Google ID token.
     * This method can be used for registration via Google.
     */
    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registrationSuccess.value = true
                    onSuccess()
                } else {
                    _registrationSuccess.value = false
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }
}
