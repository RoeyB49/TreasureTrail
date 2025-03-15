package com.example.treasuretrail.ui.post

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.treasuretrail.databinding.FragmentUploadPostBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Upload_post : Fragment() {

    private var _binding: FragmentUploadPostBinding? = null
    private val binding get() = _binding!!


    private lateinit var imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
    private var selectedImageUri: Uri? = null


    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadPostBinding.inflate(inflater, container, false)

        // Initialize image picker
        imagePickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.lostPic.setImageURI(uri)
            } else {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // When the user taps the add image button, launch the image picker
        binding.btnAddImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        // Handle post submission
        binding.btnSubmitPost.setOnClickListener {
            submitPost()
        }
    }

    private fun submitPost() {
        // Retrieve input values
        val title = binding.etPostTitle.text.toString().trim()
        val details = binding.etPostDetails.text.toString().trim()
        val location = binding.etPostLocation.text.toString().trim()

        // Get the selected category from the ChipGroup
        val selectedChipId = binding.chipGroupCategories.checkedChipId
        if (selectedChipId == View.NO_ID) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedChip = binding.chipGroupCategories.findViewById<Chip>(selectedChipId)
        val category = selectedChip?.text.toString()

        // Validate required field: title (you can add more validations as needed)
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a post title", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure the user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentUser.uid
        val postId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        // Check if an image was selected; if yes, upload it first
        if (selectedImageUri != null) {
            val storageRef = storage.reference.child("post_images/$postId.jpg")
            storageRef.putFile(selectedImageUri!!).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    createPost(postId, userId, title, category, details, location, imageUrl, timestamp)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            // No image selected, create the post directly
            createPost(postId, userId, title, category, details, location, null, timestamp)
        }
    }

    private fun createPost(
        postId: String,
        userId: String,
        title: String,
        category: String,
        details: String,
        location: String,
        imageUrl: String?,
        timestamp: Long
    ) {
        val post = hashMapOf(
            "postId" to postId,
            "userId" to userId,
            "title" to title,
            "category" to category,
            "details" to details,
            "location" to location,
            "imageUrl" to imageUrl,
            "timestamp" to timestamp
        )
        firestore.collection("posts").document(postId)
            .set(post)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Post created successfully", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
