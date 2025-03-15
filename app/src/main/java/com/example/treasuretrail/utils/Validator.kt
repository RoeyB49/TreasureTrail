package com.example.treasuretrail.utils

import android.util.Patterns

class Validator {
    private val _passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$".toRegex()
    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return _passwordPattern.matches(password)
    }

    fun validatePhoneNumber(phoneNumber: String): Boolean {
        return Patterns.PHONE.matcher(phoneNumber).matches()
    }

    fun validateUserName(userName: String): Boolean {
        return userName.isNotEmpty()
    }

    fun validateImageUri(imageUri: String): Boolean {
        return imageUri.isNotEmpty()
    }

    fun validateTitle(title: String): Boolean {
        return title.isNotEmpty()
    }

    fun validateContent(content: String): Boolean {
        return content.isNotEmpty()
    }
}