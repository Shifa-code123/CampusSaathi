package com.example.campussaathi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivityStudentProfileBinding
import com.example.campussaathi.utils.DrawerManager
import com.example.campussaathi.utils.ProfileImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Log.d("StudentProfile", "Image selected: $it")
                convertAndSaveProfileImage(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadStudentData()
    }

    private fun setupUI() {
        binding.header.tvHeaderTitle.text = "Profile"
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Setup Drawer
        DrawerManager.setupDrawer(this, binding.drawerLayout, binding.studentDrawer.root)

        // Load images in all 3 required places using ProfileImageLoader (Base64 + Real-time)
        ProfileImageLoader.loadProfile(binding.profileImage)
        ProfileImageLoader.loadProfile(binding.header.ivProfile)
        val drawerImage = binding.studentDrawer.root.findViewById<ImageView>(R.id.profileImage)
        if (drawerImage != null) {
            ProfileImageLoader.loadProfile(drawerImage)
        }

        // Image pick trigger
        binding.ivAddPhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.profileImage.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnEditProfile.setOnClickListener {
            toggleEditMode(true)
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun loadStudentData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("StudentProfile", "Error fetching user data", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val name = snapshot.getString("fullName") ?: ""
                val email = snapshot.getString("email") ?: ""
                val phone = snapshot.getString("phone") ?: ""

                binding.tvStudentName.text = name
                
                if (binding.btnEditProfile.visibility == View.VISIBLE) {
                    binding.edtName.setText(name)
                    binding.edtEmail.setText(email)
                    binding.edtPhone.setText(phone)
                }
            }
        }
    }

    /**
     * CONVERT IMAGE TO BASE64 AND SAVE (Same as Owner Profile logic)
     */
    private fun convertAndSaveProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            // Compress bitmap (JPEG, ~35 quality as in Owner logic)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 35, outputStream)

            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Save in Firestore under "profileImageBase64" (as analyzed from ActivityOwnerProfile)
            // Note: The prompt asked for "profileImage" but said "same as owner logic".
            // ActivityOwnerProfile uses "profileImageBase64". I will use that for consistency with owner logic.
            db.collection("users").document(uid)
                .update("profileImageBase64", base64Image)
                .addOnSuccessListener {
                    Log.d("StudentProfile", "Profile image Base64 saved to Firestore")
                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("StudentProfile", "Failed to save Base64", e)
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveProfileChanges() {
        val uid = auth.currentUser?.uid ?: return
        val updates = mapOf(
            "fullName" to binding.edtName.text.toString(),
            "email" to binding.edtEmail.text.toString(),
            "phone" to binding.edtPhone.text.toString()
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                toggleEditMode(false)
                Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Log.e("StudentProfile", "Failed to update profile", it)
            }
    }

    private fun toggleEditMode(isEditing: Boolean) {
        binding.edtName.isEnabled = isEditing
        binding.edtEmail.isEnabled = isEditing
        binding.edtPhone.isEnabled = isEditing

        if (isEditing) {
            binding.btnEditProfile.visibility = View.GONE
            binding.btnSaveProfile.visibility = View.VISIBLE
        } else {
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnSaveProfile.visibility = View.GONE
        }
    }
}
