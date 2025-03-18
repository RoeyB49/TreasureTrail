package com.example.treasuretrail.ui.unAuthScreens.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginSuccess.value = true
                    onSuccess()
                } else {
                    _loginSuccess.value = false
                    onFailure(task.exception?.message ?: "Authentication failed")
                }
            }
    }

    fun loginWithEmail(email: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginSuccess.value = true
                    Log.d("FirebaseAuth", "Login successful for: $email")
                    onSuccess()
                } else {
                    _loginSuccess.value = false
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Log.e("FirebaseAuth", "Login failed: $errorMessage")
                    onFailure(errorMessage)
                }
            }
    }

}