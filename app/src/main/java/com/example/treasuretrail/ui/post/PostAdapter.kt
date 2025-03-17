package com.example.treasuretrail.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.treasuretrail.R
import com.example.treasuretrail.models.Post
import com.squareup.picasso.Picasso

class PostAdapter(
    private val posts: List<Post>,
    private val onMoreInfoClicked: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.userName)
        val location: TextView = view.findViewById(R.id.postLocation)
        val title: TextView = view.findViewById(R.id.postTitle)
        val category: TextView = view.findViewById(R.id.postCategory)
//        val postImage: ImageView = view.findViewById(R.id.postImage)
        val moreInfoButton: Button = view.findViewById(R.id.moreInfoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_post_preview, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.userName.text = "Username: ${post.userName}"
        holder.location.text = "Location:${post.location }"
        holder.title.text = "details :${post.description}"
        holder.category.text = "category:${post.category}"
//        Picasso.get()
//            .load(post.lostItemImageUri)
//            .placeholder(R.drawable.lost_item)
//            .error(R.drawable.warning)
//            .into(holder.postImage)

        holder.moreInfoButton.setOnClickListener {
            onMoreInfoClicked(post)
        }
    }

    override fun getItemCount(): Int = posts.size
}



