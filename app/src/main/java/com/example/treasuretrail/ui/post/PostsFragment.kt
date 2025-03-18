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
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.ui.post.PostAdapter


class PostsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posts, container, false)

        recyclerView = view.findViewById(R.id.itemRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fetchPosts()

        return view
    }

    private fun fetchPosts() {
        db.collection("posts")
            .orderBy("timestamp")
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
                        title =  doc.getString("title") ?: "",
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
                        Log.d("PostsFragment", "Retrieved phone number: $contactInfo")
                        val postWithUser = basePost.copy(
                            userName = username,
                            imageUrl = userImgUri,
                            contactInformation = contactInfo
                        )
                        postList.add(postWithUser)
                    }
                    tasks.add(userTask)
                }

                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        postAdapter = PostAdapter(postList) { post ->
                            val bundle = Bundle().apply {
                                putSerializable("post", post)
                            }
                            view?.findNavController()
                                ?.navigate(R.id.action_PostsFragment_to_FullPostFragment, bundle)
                        }
                        recyclerView.adapter = postAdapter
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to fetch posts", Toast.LENGTH_SHORT).show()
                    }
            }

    }


}
