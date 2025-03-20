package com.example.treasuretrail.ui.unAuthScreens.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    // Image picker
    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    // Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    companion object {
        private const val TAG = "GoogleSignIn"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Google Sign-In launcher
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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

        // Setup profile image selection
        binding.editProfileIcon.setOnClickListener {
            getContentLauncher.launch("image/*")
        }

        // Initialize Google Sign-In
        setupGoogleSignIn()

        // Setup regular email/password registration
        setupEmailPasswordRegistration()

        registerViewModel.registrationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "User registered successfully!",
                    Toast.LENGTH_SHORT
                ).show()

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

    private fun setupEmailPasswordRegistration() {
        binding.btnSubmit.setOnClickListener {
            val userName = binding.inputUserName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()

            if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            registerViewModel.registerUser(
                userName,
                email,
                password,
                phone,
                selectedImageUri,
                onSuccess = {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    if (isAdded) {
                        try {
                            findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
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

            // Get ID token and authenticate with Firebase
            val idToken = account.idToken
            if (idToken != null) {
                Log.d(TAG, "Got ID token, registering with Firebase")
                binding.progressBar.visibility = View.VISIBLE

                registerViewModel.signInWithGoogle(
                    idToken,
                    onSuccess = {
                        binding.progressBar.visibility = View.GONE
                        Log.d(TAG, "Firebase registration successful")
                        Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
                    },
                    onFailure = { errorMessage ->
                        binding.progressBar.visibility = View.GONE
                        Log.e(TAG, "Firebase registration failed: $errorMessage")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Log.e(TAG, "No ID token received")
                Toast.makeText(requireContext(), "Registration failed: No ID token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
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