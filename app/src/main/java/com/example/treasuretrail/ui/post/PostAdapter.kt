package com.example.treasuretrail.ui.post

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.treasuretrail.R
import com.example.treasuretrail.models.Post
import com.google.firebase.auth.FirebaseAuth

class PostAdapter(
    private val posts: MutableList<Post>,
    private val onMoreInfoClicked: (Post) -> Unit,
    private val onDeleteClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val auth = FirebaseAuth.getInstance() // Get current user ID

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val location: TextView = view.findViewById(R.id.postLocation)
        val title: TextView = view.findViewById(R.id.postTitle)
        val category: TextView = view.findViewById(R.id.postCategory)
        val moreInfoButton: Button = view.findViewById(R.id.moreInfoButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        val editDeleteContainer: View = view.findViewById(R.id.editDeleteContainer) // Container for delete/edit
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_post_preview, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val currentUserId = auth.currentUser?.uid

        holder.userName.text = "Username: ${post.userName}"
        holder.location.text = "Location: ${post.location}"
        holder.title.text = "Title: ${post.title}"
        holder.category.text = "Category: ${post.category}"

        holder.moreInfoButton.setOnClickListener {
            onMoreInfoClicked(post)
        }

        // Only show delete button if the post belongs to the current user
        if (post.userId == currentUserId) {
            holder.editDeleteContainer.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClicked(post)
            }
        } else {
            holder.editDeleteContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = posts.size

    fun removePost(post: Post) {
        val index = posts.indexOf(post)
        if (index != -1) {
            posts.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
