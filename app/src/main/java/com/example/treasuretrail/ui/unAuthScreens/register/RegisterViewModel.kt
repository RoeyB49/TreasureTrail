package com.example.treasuretrail.ui.unAuthScreens.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class RegisterViewModel : ViewModel() {
    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> = _registrationSuccess

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    _registrationSuccess.value = true
                    onSuccess()
                } else {
                    // Sign in fails
                    _registrationSuccess.value = false
                    onFailure(task.exception?.message ?: "Registration failed")
                }
            }
    }
}