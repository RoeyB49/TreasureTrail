package com.example.treasuretrail.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.treasuretrail.R
import com.example.treasuretrail.data.repository.UserRepository
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class EditProfile : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var inputUserName: TextInputEditText
    private lateinit var inputPhone: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: CircularProgressIndicator

    private var imageUri: Uri? = null
    private val userRepository = UserRepository()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            imageUri = it.data?.data
            profileImageView.setImageURI(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = view.findViewById(R.id.profileImage)
        inputUserName = view.findViewById(R.id.inputUserName)
        inputPhone = view.findViewById(R.id.inputPhone)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        btnCancel = view.findViewById(R.id.btnCancel)
        progressBar = view.findViewById(R.id.progress_bar)

        btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        profileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        loadExistingUserData()

        btnSubmit.setOnClickListener { updateUserData() }
    }

    private fun loadExistingUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        userRepository.getUserData(userId, { user ->
            inputUserName.setText(user.username)
            inputPhone.setText(user.phoneNumber)

            if (!user.imageUri.isNullOrEmpty()) {
                Picasso.get().load(user.imageUri)
                    .placeholder(R.drawable.avatar_default)
                    .into(profileImageView)
            }
        }, {
            Toast.makeText(context, "Error loading user data", Toast.LENGTH_SHORT).show()
        })
    }

    private fun updateUserData() {
        progressBar.visibility = View.VISIBLE

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val newUserName = inputUserName.text.toString().trim()
        val newPhoneNumber = inputPhone.text.toString().trim()

        userRepository.updateUserProfile(
            userId,
            username = newUserName.ifEmpty { null },
            phoneNumber = newPhoneNumber.ifEmpty { null },
            imageUri = imageUri,
            onSuccess = {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            },
            onFailure = {
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
