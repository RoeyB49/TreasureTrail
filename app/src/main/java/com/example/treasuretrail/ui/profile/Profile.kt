package com.example.treasuretrail.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.data.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

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
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivProfileAvatar = view.findViewById(R.id.ivProfileAvatar)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        tvUserPhone = view.findViewById(R.id.tvUserPhone)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnMyPosts = view.findViewById(R.id.btnMyPosts)
        btnLogout = view.findViewById(R.id.btnLogout)

        // Load user data
        loadUserData()

        setupButtonListeners()
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRepository = UserRepository()

        userId?.let { id ->
            userRepository.getUserData(id, { user ->
                // Update UI
                tvUserName.text = "Username: ${user.username}"
                tvUserEmail.text = "Email: ${user.email}"
                tvUserPhone.text = "Phone Number: ${user.phoneNumber}"

                // Log all the fields
                Log.d("ProfileFragment", "Fetched user - Username: ${user.username}, Email: ${user.email}, Phone: ${user.phoneNumber}, Image URI: ${user.imageUri}")

                if (!user.imageUri.isNullOrEmpty()) {
                    Picasso.get()
                        .load(user.imageUri)
                        .placeholder(R.drawable.avatar_default)
                        .error(R.drawable.avatar_default)
                        .into(ivProfileAvatar)
                } else {
                    ivProfileAvatar.setImageResource(R.drawable.avatar_default)
                }
            }, { exception ->
                Log.e("ProfileFragment", "Error fetching user data", exception)
            })
        }
    }

    private fun setupButtonListeners() {
        btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        btnMyPosts.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_mypostsFragment)
        }

        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()

        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_profileFragment_to_welcomeFragment)
    }


}