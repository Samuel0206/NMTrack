package com.project.tracker.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.project.tracker.ActivityFeedback
import com.project.tracker.ActivitySetting
import com.project.tracker.EditProfileActivity
import com.project.tracker.R

class UserPageFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // 添加视图绑定
    private lateinit var imgAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnFeedback: Button
    private lateinit var btnProfileSetting: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_user_page_fragment, container, false)

        // 初始化视图绑定
        imgAvatar = view.findViewById(R.id.imgAvatar)
        tvUsername = view.findViewById(R.id.tvUsername)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnFeedback = view.findViewById(R.id.btnFeedBack)
        btnProfileSetting = view.findViewById(R.id.btnProfileSetting)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Fetch and display the username from the database
        getCurrentUserDisplayNameFromDatabase()


        // Set the avatar image (you can customize this based on your requirements)
        imgAvatar.setImageResource(R.drawable.defaultavatar)

        // Clicking the edit button to start the profile edit activity/fragment
        btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)

            // Pass the current username as an extra to EditProfileActivity
            val currentUsername = tvUsername.text.toString()
            intent.putExtra("currentUsername", currentUsername)

            startActivity(intent)
        }

        btnFeedback.setOnClickListener {
            startActivity(Intent(requireContext(), ActivityFeedback::class.java))
        }

        btnProfileSetting.setOnClickListener {
            startActivity(Intent(requireContext(), ActivitySetting::class.java))
        }


    }

    private fun getCurrentUserDisplayNameFromDatabase() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val userRef = database.child("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)

                    // Check if the retrieved username is not null or empty before updating tvUsername
                    if (!username.isNullOrBlank()) {
                        tvUsername.text = username
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            }

            userRef.addValueEventListener(valueEventListener)

        }
    }



}