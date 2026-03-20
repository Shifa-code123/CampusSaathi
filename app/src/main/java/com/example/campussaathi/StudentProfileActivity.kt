package com.example.campussaathi

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivityStudentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide
import com.example.campussaathi.utils.DrawerManager

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding

    private var isEditing = false
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 📸 Image Picker
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {

                Glide.with(this)
                    .load(it)
                    .circleCrop()
                    .into(binding.profileImage)

                uploadProfileImage(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Drawer setup
        DrawerManager.setupDrawer(
            this,
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        binding.header.tvHeaderTitle.text = "Profile"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // ➕ Select profile image
        binding.ivAddPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.profileImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Edit button
        binding.btnEditProfile.setOnClickListener {
            enableEditMode()
        }

        // Save button
        binding.btnSaveProfile.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("Do you want to save changes?")
                .setPositiveButton("Yes") { _, _ ->
                    saveProfileChanges()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    exitEditMode()
                }
                .show()
        }

        loadStudentData()
    }

    // ===============================
    // LOAD PROFILE DATA
    // ===============================

    private fun loadStudentData() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->

                if (document.exists()) {

                    val name = document.getString("fullName") ?: ""
                    val email = document.getString("email") ?: ""
                    val phone = document.getString("phone") ?: ""
                    val imageUrl = document.getString("profileImage")

                    binding.tvStudentName.text = name
                    binding.edtName.setText(name)
                    binding.edtEmail.setText(email)
                    binding.edtPhone.setText(phone)

                    if (!imageUrl.isNullOrEmpty()) {

                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(binding.profileImage)

                        // Header profile image
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(binding.header.ivProfile)
                    }
                }
            }
    }

    // ===============================
    // ENABLE EDIT MODE
    // ===============================

    private fun enableEditMode() {

        isEditing = true

        binding.edtName.isEnabled = true
        binding.edtEmail.isEnabled = true
        binding.edtPhone.isEnabled = true

        binding.edtName.isFocusable = true
        binding.edtName.isFocusableInTouchMode = true
        binding.edtName.requestFocus()

        binding.btnEditProfile.visibility = View.GONE
        binding.btnSaveProfile.visibility = View.VISIBLE
    }

    // ===============================
    // EXIT EDIT MODE
    // ===============================

    private fun exitEditMode() {

        isEditing = false

        binding.edtName.isEnabled = false
        binding.edtEmail.isEnabled = false
        binding.edtPhone.isEnabled = false

        binding.btnSaveProfile.visibility = View.GONE
        binding.btnEditProfile.visibility = View.VISIBLE
    }

    // ===============================
    // SAVE PROFILE CHANGES
    // ===============================

    private fun saveProfileChanges() {

        val uid = auth.currentUser?.uid ?: return

        val newName = binding.edtName.text.toString()
        val newEmail = binding.edtEmail.text.toString()
        val newPhone = binding.edtPhone.text.toString()

        val updates = mapOf(
            "fullName" to newName,
            "email" to newEmail,
            "phone" to newPhone
        )

        db.collection("users")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {

                binding.tvStudentName.text = newName

                exitEditMode()
            }
    }

    // ===============================
    // UPLOAD PROFILE IMAGE
    // ===============================

    private fun uploadProfileImage(uri: Uri) {

        val uid = auth.currentUser?.uid ?: return

        val storageRef = FirebaseStorage.getInstance()
            .reference.child("profileImages/$uid.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {

                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->

                    val imageUrl = downloadUrl.toString()

                    db.collection("users")
                        .document(uid)
                        .update("profileImage", imageUrl)

                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(binding.profileImage)

                    Glide.with(this)
                        .load(imageUrl)
                        .circleCrop()
                        .into(binding.header.ivProfile)
                }
            }
    }
}