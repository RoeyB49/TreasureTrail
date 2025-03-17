package com.example.treasuretrail.ui.unAuthScreens.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.databinding.FragmentLoginBinding
import com.example.treasuretrail.ui.unAuthScreens.login.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "Activity result received: ${result.resultCode}")
            if (result.data == null) {
                Log.e(TAG, "Sign-in intent returned null data")
                return@registerForActivityResult
            }
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        setupGoogleSignIn()

        loginViewModel.loginSuccess.observe(viewLifecycleOwner) { success ->
            super.onViewCreated(view, savedInstanceState)

            emailLayout = view.findViewById(R.id.layoutEmail)
            emailInput = view.findViewById(R.id.inputEmail)
            passwordLayout = view.findViewById(R.id.layoutPassword)
            passwordInput = view.findViewById(R.id.etPassword)
            loginButton = view.findViewById(R.id.btnSubmitLogin)
            progressBar = view.findViewById(R.id.progressBar)

            loginButton.setOnClickListener {
                handleLogin()
            }

            if (success && isAdded) {
                try {
                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation error: ${e.message}")
                }
            }
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


        Handler(Looper.getMainLooper()).postDelayed({
            progressBar.visibility = View.GONE
            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
        }, 2000)
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("549062980367-m4l2g908rukbn5ge4i61okjltje2hqld.apps.googleusercontent.com")
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            binding.btnGoogleSignIn.setOnClickListener {
                try {
                    Log.d(TAG, "Starting Google Sign-In flow")
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting sign-in: ${e.message}")
                    Toast.makeText(requireContext(), "Error starting sign-in", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Setup error: ${e.message}")
            Toast.makeText(requireContext(), "Error setting up Google Sign-In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful, email: ${account.email}")
            val idToken = account.idToken

            if (idToken != null) {
                Log.d(TAG, "Got ID token, authenticating with Firebase")
                loginViewModel.signInWithGoogle(
                    idToken,
                    onSuccess = {
                        Log.d(TAG, "Firebase authentication successful")
                        Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                        if (isAdded) {
                            try {
                                findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                            } catch (e: Exception) {
                                Log.e(TAG, "Navigation error: ${e.message}")
                            }
                        }
                    },
                    onFailure = { errorMessage ->
                        Log.e(TAG, "Firebase authentication failed: $errorMessage")
                        if (isAdded) {
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                Log.e(TAG, "No ID token received")
                if (isAdded) {
                    Toast.makeText(requireContext(), "Authentication failed: No ID token", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Sign-in failed, code: ${e.statusCode}, message: ${e.message}")
            val errorMessage = when (e.statusCode) {
                12500 -> "This device doesn't have Google Play Services"
                12501 -> "User canceled the sign-in flow"
                12502 -> "Could not resolve sign-in intent"
                else -> "Sign-in failed: code ${e.statusCode}"
            }
            if (isAdded) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}