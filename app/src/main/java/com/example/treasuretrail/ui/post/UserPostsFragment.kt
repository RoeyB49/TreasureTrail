package com.example.treasuretrail.ui.post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.treasuretrail.R
import com.google.firebase.firestore.FirebaseFirestore
import com.example.treasuretrail.models.Post
import com.google.firebase.auth.FirebaseAuth

class UserPostsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posts, container, false)

        recyclerView = view.findViewById(R.id.itemRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchUserPosts()

        return view
    }

    private fun fetchUserPosts() {
        // Get current user ID
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(context, "You need to be logged in to view your posts", Toast.LENGTH_SHORT).show()
            return
        }

        // Query only posts created by the current user without ordering by timestamp
        db.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val postList = mutableListOf<Post>()
                val tasks =
                    mutableListOf<com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot>>()

                for (doc in documents) {
                    val basePost = Post(
                        id = doc.getString("postId") ?: doc.id,
                        userName = "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        location = doc.getString("location") ?: "",
                        category = doc.getString("category") ?: "",
                        details = doc.getString("details") ?: "",
                        title = doc.getString("title") ?: "",
                        contactInformation = "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        userId = doc.getString("userId") ?: ""
                    )

                    val userId = doc.getString("userId") ?: ""

                    val userTask = db.collection("users").document(userId).get()
                    userTask.addOnSuccessListener { userDoc ->
                        val username = userDoc.getString("username") ?: "Unknown"
                        val userImgUri = userDoc.getString("imageUri") ?: ""
                        val contactInfo = userDoc.getString("phoneNumber") ?: ""
                        Log.d("UserPostsFragment", "Retrieved phone number: $contactInfo")
                        val postWithUser = basePost.copy(
                            userName = username,
                            imageUrl = userImgUri,
                            contactInformation = contactInfo
                        )
                        postList.add(postWithUser)

                        // Check if this is the last task and sort the list
                        if (postList.size == documents.size()) {
                            // Sort by timestamp descending (newest first)
                            val sortedList = postList.sortedByDescending { it.timestamp }
                            displayPosts(sortedList)
                        }
                    }
                    tasks.add(userTask)
                }

                // Handle empty result case
                if (documents.isEmpty) {
                    Toast.makeText(context, "You haven't created any posts yet", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserPostsFragment", "Error getting posts: ", exception)
                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayPosts(posts: List<Post>) {
        if (posts.isEmpty()) {
            Toast.makeText(context, "You haven't created any posts yet", Toast.LENGTH_SHORT).show()
        }

        postAdapter = PostAdapter(posts.toMutableList(),
            onMoreInfoClicked = { post ->
                val bundle = Bundle().apply {
                    putSerializable("post", post)
                }
                view?.findNavController()
                    ?.navigate(R.id.action_UserPostsFragment_to_FullPostFragment, bundle)
            },
            onDeleteClicked = { post ->
                deletePost(post)
            }
        )
        recyclerView.adapter = postAdapter
    }

    private fun deletePost(post: Post) {
        db.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                postAdapter.removePost(post) // Update UI
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}