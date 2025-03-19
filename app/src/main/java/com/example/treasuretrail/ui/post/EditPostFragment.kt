package com.example.treasuretrail.ui.post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.treasuretrail.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.treasuretrail.models.Post
import com.google.android.material.textfield.TextInputEditText

import androidx.navigation.fragment.findNavController

class EditPostFragment : Fragment() {
    private val TAG = "EditPostFragment"
    private lateinit var titleEditText: TextInputEditText
    private lateinit var detailsEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var updateButton: Button
    private val db = FirebaseFirestore.getInstance()

    // Initialize post as null to avoid initialization issues
    private var post: Post? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView started")
        val view = inflater.inflate(R.layout.fragment_edit_post, container, false)

        // Initialize views
        titleEditText = view.findViewById(R.id.etTitle)
        detailsEditText = view.findViewById(R.id.etDetails)

        locationEditText = view.findViewById(R.id.etLocation)
        updateButton = view.findViewById(R.id.btnSaveChanges)

        // Get the post object from arguments
        try {
            arguments?.let { args ->
                Log.d(TAG, "Arguments received: ${args.keySet().joinToString()}")
                if (args.containsKey("post")) {
                    // Try to get as Serializable (safer approach)
                    post = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        args.getSerializable("post", Post::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        args.getSerializable("post") as? Post
                    }

                    Log.d(TAG, "Post retrieved: ${post?.title}")

                    // Pre-fill the fields with the post data
                    post?.let { loadPostData(it) }
                } else {
                    Log.e(TAG, "No post key in arguments")
                    Toast.makeText(context, "Post data not available", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            } ?: run {
                Log.e(TAG, "Arguments is null")
                Toast.makeText(context, "Arguments not available", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading post data: ${e.message}", e)
            Toast.makeText(context, "Error loading post data: ${e.message}", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        updateButton.setOnClickListener {
            savePostChanges()
        }

        return view
    }

    private fun loadPostData(post: Post) {
        titleEditText.setText(post.title)
        detailsEditText.setText(post.details)
        locationEditText.setText(post.location)

    }

    private fun savePostChanges() {
        val currentPost = post
        if (currentPost == null) {
            Toast.makeText(context, "Post data not available", Toast.LENGTH_SHORT).show()
            return
        }

        val title = titleEditText.text.toString().trim()
        val details = detailsEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()


        // Validate input
        if (title.isEmpty() || details.isEmpty() || location.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map of updated post data
        val updatedPostMap = hashMapOf(
            "title" to title,
            "details" to details,
            "location" to location
        )

        // Show loading indicator or disable button
        updateButton.isEnabled = false
        updateButton.text = "Updating..."

        db.collection("posts").document(currentPost.id)
            .update(updatedPostMap as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating post: ${e.message}", e)
                Toast.makeText(context, "Error updating post: ${e.message}", Toast.LENGTH_SHORT).show()
                // Re-enable button
                updateButton.isEnabled = true
                updateButton.text = "Save Changes"
            }
    }


}