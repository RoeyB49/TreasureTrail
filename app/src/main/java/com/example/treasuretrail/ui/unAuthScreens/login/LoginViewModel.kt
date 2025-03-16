package com.example.treasuretrail.ui.unAuthScreens.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel : ViewModel() {
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    _loginSuccess.value = true
                    onSuccess()
                } else {
                    // Sign in fails
                    _loginSuccess.value = false
                    onFailure(task.exception?.message ?: "Authentication failed")
                }
            }
    }
}