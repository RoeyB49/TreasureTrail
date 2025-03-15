package com.example.treasuretrail.ui.unAuthScreens.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.treasuretrail.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Login : Fragment() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment (make sure the layout name is correct)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using the inflated view
        emailLayout = view.findViewById(R.id.layoutEmail)
        emailInput = view.findViewById(R.id.inputEmail)
        passwordLayout = view.findViewById(R.id.layoutPassword)
        passwordInput = view.findViewById(R.id.etPassword)
        loginButton = view.findViewById(R.id.btnSubmitLogin)
        progressBar = view.findViewById(R.id.progressBar)

        // Set click listener on the login button
        loginButton.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val email = emailInput.text?.toString()?.trim() ?: ""
        val password = passwordInput.text?.toString()?.trim() ?: ""

        // Validate email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Enter a valid email"
            return
        } else {
            emailLayout.error = null
        }

        // Validate password
        if (password.isEmpty() || password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            return
        } else {
            passwordLayout.error = null
        }

        // Show progress bar while "logging in"
        progressBar.visibility = View.VISIBLE

        // Simulate login process (replace this with your authentication logic)
        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
            // Navigate to next screen or perform other actions upon successful login
        }, 2000) // 2-second delay to simulate network call
    }

    companion object {
        // Factory method to create a new instance of this fragment if needed.
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Login().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
