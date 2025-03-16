package com.example.treasuretrail.ui.unAuthScreens.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class Register : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        // Initialize Google Sign-In
        setupGoogleSignIn()

        // Observe registration success
        registerViewModel.registrationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "User registered successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                // Navigate to profile fragment
                findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Registration failed. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
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
                // Clear any previous sign-in state
                googleSignInClient.signOut().addOnCompleteListener {
                    try {
                        Log.d(TAG, "Starting Google Sign-In flow")
                        val signInIntent = googleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RC_SIGN_IN)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting sign-in: ${e.message}")
                        Toast.makeText(requireContext(), "Error starting sign-in", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Setup error: ${e.message}")
            Toast.makeText(requireContext(), "Error setting up Google Sign-In", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "Got sign-in result")
            // Handle Google Sign-In result
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful, email: ${account?.email}")

            // Get ID token and authenticate with Firebase
            val idToken = account?.idToken
            if (idToken != null) {
                Log.d(TAG, "Got ID token, registering with Firebase")
                registerViewModel.signInWithGoogle(
                    idToken,
                    onSuccess = {
                        Log.d(TAG, "Firebase registration successful")
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                        // Navigate to profile fragment
                        findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
                    },
                    onFailure = { errorMessage ->
                        Log.e(TAG, "Firebase registration failed: $errorMessage")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Log.e(TAG, "No ID token received")
                Toast.makeText(requireContext(), "Registration failed: No ID token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason
            Log.e(TAG, "Sign-in failed, code: ${e.statusCode}, message: ${e.message}")
            val errorMessage = when (e.statusCode) {
                12500 -> "This device doesn't have Google Play Services"
                12501 -> "User canceled the sign-in flow"
                12502 -> "Could not resolve sign-in intent"
                else -> "Sign-in failed: code ${e.statusCode}"
            }
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}