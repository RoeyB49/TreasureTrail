package com.example.treasuretrail.ui.unAuthScreens.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class Login : Fragment() {
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

        // Setup Google Sign-In
        setupGoogleSignIn()

        // Setup regular email/password login
        setupEmailPasswordLogin()

        // Observe login success
        loginViewModel.loginSuccess.observe(viewLifecycleOwner) { success ->
            if (success && isAdded) {
                try {
                    findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation error: ${e.message}")
                }
            }
        }
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

    private fun setupEmailPasswordLogin() {
        binding.btnSubmitLogin.setOnClickListener {
            val email = binding.inputEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString()?.trim() ?: ""

            // Validate email
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.layoutEmail.error = "Enter a valid email"
                return@setOnClickListener
            } else {
                binding.layoutEmail.error = null
            }

            // Validate password
            if (password.isEmpty() || password.length < 6) {
                binding.layoutPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            } else {
                binding.layoutPassword.error = null
            }

            // Show progress bar
            binding.progressBar.visibility = View.VISIBLE

            // Call the ViewModel to handle login
            loginViewModel.loginWithEmail(
                email,
                password,
                onSuccess = {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    if (isAdded) {
                        try {
                            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
                        } catch (e: Exception) {
                            Log.e(TAG, "Navigation error: ${e.message}")
                        }
                    }
                },
                onFailure = { errorMessage ->
                    binding.progressBar.visibility = View.GONE
                    if (isAdded) {
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful, email: ${account.email}")
            val idToken = account.idToken

            if (idToken != null) {
                Log.d(TAG, "Got ID token, authenticating with Firebase")
                binding.progressBar.visibility = View.VISIBLE

                loginViewModel.signInWithGoogle(
                    idToken,
                    onSuccess = {
                        binding.progressBar.visibility = View.GONE
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
                        binding.progressBar.visibility = View.GONE
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