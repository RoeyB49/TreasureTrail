package com.example.treasuretrail.ui.unAuthScreens.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.treasuretrail.utils.Validator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    val email = MutableLiveData("")
    val password = MutableLiveData("")

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess


    val isEmailValid = MutableLiveData(true)
    val isPasswordValid = MutableLiveData(true)

    val isFormValid: Boolean
        get() = isEmailValid.value!! && isPasswordValid.value!!
    private val validator = Validator()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun login(onSuccess: () -> Unit, onFailure: (error: Exception?) -> Unit) {
        validateForm()
        if (!isFormValid) {
            onFailure(null)
            return
        }

        signInUserConcurrently(onSuccess, onFailure)
    }

    private fun validateForm() {
        isEmailValid.value = validator.validateEmail(email.value!!)
        isPasswordValid.value = validator.validatePassword(password.value!!)
    }

    private fun signInUserConcurrently(
        onSuccess: () -> Unit,
        onFailure: (error: Exception?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    auth.signInWithEmailAndPassword(email.value!!, password.value!!).await()
                }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("Login", "Error signing in user", e)
                withContext(Dispatchers.Main) { onFailure(e) }
            }
        }
    }

    fun signInWithGoogle(
        idToken: String,
        onSuccess: () -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
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