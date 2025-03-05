package com.example.treasuretrail.ui.unAuthScreens.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasuretrail.utils.Validator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    val email = MutableLiveData("")
    val password = MutableLiveData("")

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

    private fun validateForm() {
        isEmailValid.value = validator.validateEmail(email.value!!)
        isPasswordValid.value = validator.validatePassword(password.value!!)
    }
}