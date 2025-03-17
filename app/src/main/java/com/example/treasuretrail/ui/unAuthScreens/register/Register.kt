package com.example.treasuretrail.ui.unAuthScreens.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.databinding.FragmentRegisterBinding

class Register : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Image picker launcher and selected image URI
    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    private lateinit var registerViewModel: RegisterViewModel

    companion object {
        private const val TAG = "Register"
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        // Initialize image picker
        getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.profileImage.setImageURI(it)
            } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        // Observe registration success
        registerViewModel.registrationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "User registered successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
            } else {
                Toast.makeText(requireContext(), "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // When the edit icon is clicked, launch the image picker
        binding.editProfileIcon.setOnClickListener {
            getContentLauncher.launch("image/*")
        }

        // Set up the submit button to register the user with the provided fields
        binding.btnSubmit.setOnClickListener {
            val userName = binding.inputUserName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()
            val imageUri = selectedImageUri

            if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            registerViewModel.registerUser(userName, email, password, phone, imageUri)
        }

        // Set up Google Sign-In
        setupGoogleSignIn()
    }

    private fun setupGoogleSignIn() {
        try {
            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            )
                .requestIdToken("549062980367-m4l2g908rukbn5ge4i61okjltje2hqld.apps.googleusercontent.com")
                .requestEmail()
                .build()

            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(requireActivity(), gso)

            binding.btnGoogleSignIn.setOnClickListener {
                // Clear any previous sign-in state before starting a new one.
                googleSignInClient.signOut().addOnCompleteListener {
                    try {
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
            handleSignInResult(com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data))
        }
    }

    private fun handleSignInResult(completedTask: com.google.android.gms.tasks.Task<com.google.android.gms.auth.api.signin.GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(com.google.android.gms.common.api.ApiException::class.java)
            Log.d(TAG, "Sign-in successful, email: ${account?.email}")

            val idToken = account?.idToken
            if (idToken != null) {
                Log.d(TAG, "Got ID token, registering with Firebase")
                registerViewModel.signInWithGoogle(
                    idToken,
                    onSuccess = {
                        Log.d(TAG, "Firebase registration successful")
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
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
        } catch (e: com.google.android.gms.common.api.ApiException) {
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
