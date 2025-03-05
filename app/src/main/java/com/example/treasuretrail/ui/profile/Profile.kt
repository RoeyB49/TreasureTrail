package com.example.treasuretrail.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.treasuretrail.R
import com.google.android.material.button.MaterialButton

class Profile : Fragment() {

    private lateinit var ivProfileAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnMyPosts: MaterialButton
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserPhone = view.findViewById(R.id.tvUserPhone)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnMyPosts = view.findViewById(R.id.btnMyPosts)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Example: Load user data from a shared source or arguments
        // For now, we’ll hardcode some sample data:
        val currentUserName = "John Doe"
        val currentUserEmail = "john.doe@example.com"
        val currentUserPhone = "058-290-2097"

        // Update UI
        tvUserName.text = currentUserName
        tvUserEmail.text = "Email : $currentUserEmail"
        tvUserPhone.text = "Phone Number: $currentUserPhone"

        // If you have an avatar URL or local path, load it here (e.g., with Glide or Picasso)
        // Glide.with(this).load(avatarUrl).into(ivProfileAvatar)

        // Button click listeners
        btnEditProfile.setOnClickListener {
            // Navigate to Edit Profile screen
            // For example:
            // findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        btnMyPosts.setOnClickListener {
            // Navigate to My Posts screen
            // findNavController().navigate(R.id.action_profileFragment_to_myPostsFragment)
        }

        btnLogout.setOnClickListener {
            // Perform logout logic
            // e.g., clear user session, navigate to login screen
        }
    }
}
