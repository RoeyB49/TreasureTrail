package com.example.treasuretrail.ui.post

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.models.Post
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class EditPostFragment : Fragment() {
    private val TAG = "EditPostFragment"
    private lateinit var titleEditText: TextInputEditText
    private lateinit var detailsEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var updateButton: Button
    private lateinit var postImageView: ImageView
    private lateinit var editImageButton: ImageButton
    private val db = FirebaseFirestore.getInstance()

    private var post: Post? = null
    private val PICK_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null

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
        postImageView = view.findViewById(R.id.imgPost)
        editImageButton = view.findViewById(R.id.btnEditImage)

        // Set up image selection when edit button is clicked
        editImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Get the post object from arguments
        try {
            arguments?.let { args ->
                Log.d(TAG, "Arguments received: ${args.keySet().joinToString()}")
                if (args.containsKey("post")) {
                    post = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        args.getSerializable("post", Post::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        args.getSerializable("post") as? Post
                    }
                    Log.d(TAG, "Post retrieved: ${post?.title}")
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
        // Load the current image if available
        if (post.imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(post.imageUrl)
                .placeholder(R.drawable.lost_item)
                .error(R.drawable.warning)
                .into(postImageView)
        } else {
            postImageView.setImageResource(R.drawable.lost_item)
        }
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

        // If a new image has been selected, upload it first
        if (selectedImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("post_images").child(currentPost.id)
            storageRef.putFile(selectedImageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    storageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        updatedPostMap["imageUrl"] = downloadUri
                        updatePostInFirestore(updatedPostMap, currentPost.id)
                    } else {
                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                        updateButton.isEnabled = true
                        updateButton.text = "Save Changes"
                    }
                }
        } else {
            // No new image selected, update text fields only
            updatePostInFirestore(updatedPostMap, currentPost.id)
        }
    }

    private fun updatePostInFirestore(updatedPostMap: Map<String, Any>, postId: String) {
        db.collection("posts").document(postId)
            .update(updatedPostMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating post: ${e.message}", e)
                Toast.makeText(context, "Error updating post: ${e.message}", Toast.LENGTH_SHORT).show()
                updateButton.isEnabled = true
                updateButton.text = "Save Changes"
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            // Show the selected image in the ImageView
            postImageView.setImageURI(selectedImageUri)
        }
    }
}
