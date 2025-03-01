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
import com.example.treasuretrail.R
import com.example.treasuretrail.databinding.FragmentRegisterBinding

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

        // Launch the image picker when the edit icon is clicked
        binding.editProfileIcon.setOnClickListener {
            getContentLauncher.launch("image/*")
        }

        // Handle the Join Now button click
        binding.btnSubmit.setOnClickListener {
            // Retrieve data from the input fields
            val userName = binding.inputUserName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val phone = binding.inputPhone.text.toString().trim()
            val imageUriString = selectedImageUri?.toString() ?: ""

            // Basic validation to check required fields
            if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // For demonstration, log the collected data
            Log.d("RegisterFragment", "Name: $userName")
            Log.d("RegisterFragment", "Email: $email")
            Log.d("RegisterFragment", "Password: $password")
            Log.d("RegisterFragment", "Phone: $phone")
            Log.d("RegisterFragment", "Image URI: $imageUriString")

            // Here you can store the data locally, for example using Room.
            // You could create a User entity and insert it into your database.
            // Example:
            // val user = User(userName, email, password, phone, imageUriString)
            // lifecycleScope.launch { AppDatabase.getInstance(requireContext()).userDao().insertUser(user) }

            Toast.makeText(requireContext(), "Registration data collected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
