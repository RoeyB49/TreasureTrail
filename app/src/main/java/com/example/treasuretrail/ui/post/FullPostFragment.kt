package com.example.treasuretrail.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.treasuretrail.R
import com.example.treasuretrail.models.Post
import com.squareup.picasso.Picasso

class FullPostFragment : Fragment() {

    private lateinit var post: Post

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assume the Post is passed as a Serializable
        post = requireArguments().getSerializable("post") as Post
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_full_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize views from fragment_full_post.xml
        val userNameText: TextView = view.findViewById(R.id.userName)
        val postLocationText: TextView = view.findViewById(R.id.postLocation)
        val userProfileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val postImage: ImageView = view.findViewById(R.id.postImage)
        val postTitleText: TextView = view.findViewById(R.id.postTitle)
        val postCategoryText: TextView = view.findViewById(R.id.postCategory)
        val contactInfoText: TextView = view.findViewById(R.id.contactInfo)
        val moreInfoText: TextView = view.findViewById(R.id.moreInfo)

        // Bind the post data to the views
        userNameText.text = post.userName

        postLocationText.text = "found in: ${post.location}"
        postTitleText.text = "Tiltle: ${post.title}"
        postCategoryText.text = "Category: ${post.category}"
        contactInfoText.text = "Contact: ${post.contactInformation}"
        //i need user phone number
        contactInfoText.text = "Contact: ${post.contactInformation}"
        moreInfoText.text ="Description:${post.description}"

        if (post.lostItemImageUri.isNotEmpty()) {
            Picasso.get()
                .load(post.lostItemImageUri)
                .placeholder(R.drawable.lost_item)
                .error(R.drawable.warning)
                .into(postImage)
        } else {
            postImage.setImageResource(R.drawable.lost_item)
        }
    }
}
