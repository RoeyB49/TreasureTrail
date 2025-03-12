package com.example.treasuretrail.ui.unAuthScreens.register

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.treasuretrail.R

import com.example.treasuretrail.databinding.FragmentRegisterBinding
import java.util.UUID

import com.example.treasuretrail.models.User

class Register : Fragment() {
    // Using view binding for easier access to your views
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Launcher to pick an image from the gallery
    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        // Register the launcher for the image picker
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

        val registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)

        registerViewModel.registrationSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "User registered successfully!", Toast.LENGTH_SHORT).show()
                // Navigate to login screen or home
            } else {
                Toast.makeText(requireContext(), "Registration failed. Try again.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSubmit.setOnClickListener {
            val userName = binding.inputUserName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()
            val imageUriString = selectedImageUri?.toString() ?: ""


            if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE  // Show loading indicator
            registerViewModel.registerUser(userName, email, password, phone, selectedImageUri)
        }
    }

}


