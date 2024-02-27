package com.project.tracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.tracker.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private val view: ActivityEditProfileBinding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(view.root)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Set the current username
        val currentUsername = intent.getStringExtra("currentUsername")
        view.etUsername.hint = currentUsername

        // Handle avatar click to show options
        view.imgAvatar.setOnClickListener {
            showImageOptions()
        }

        // Handle confirm button click
        view.btnSubmit.setOnClickListener {
            confirmChanges()
        }
    }

    private fun showImageOptions() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> chooseFromGallery()
                1 -> takePhoto()
            }
        }
        builder.show()
    }

    private fun chooseFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun confirmChanges() {
        val newUsername = view.etUsername.text.toString()

        updateUsernameInFirebase(newUsername)

    }


    private fun updateUsernameInFirebase(newUsername: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.child("username").setValue(newUsername)
            // 完成后关闭页面
            finish()

        }
    }

    private fun updateAvatarUrl(avatarUrl: String) {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        userId?.let {
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
            userRef.child("avatarUrl").setValue(avatarUrl)
            // 完成后关闭页面
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        imageUri = uri
                        view.imgAvatar.setImageURI(uri)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    imageUri?.let {
                        view.imgAvatar.setImageURI(it)
                    }
                }
            }
        }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
    }
}