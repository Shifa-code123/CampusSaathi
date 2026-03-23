package com.example.campussaathi

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivityStudentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.campussaathi.utils.DrawerManager
import java.io.File
import com.google.firebase.firestore.SetOptions
import android.widget.Toast
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import com.example.campussaathi.utils.ProfileImageLoader

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var cameraImageUri: Uri

    // ===============================
    // GALLERY
    // ===============================
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                convertAndSaveProfileImage(it)
            }
        }

    // ===============================
    // CAMERA
    // ===============================
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                convertAndSaveProfileImage(cameraImageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProfileImageLoader.loadProfile(binding.header.ivProfile)

        DrawerManager.setupDrawer(
            this,
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        val drawerImage = binding.studentDrawer.root
            .findViewById<ImageView>(R.id.profileImage)

        ProfileImageLoader.loadProfile(drawerImage)

        binding.header.tvHeaderTitle.text = "Profile"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.ivAddPhoto.setOnClickListener {
            showImagePickerDialog()
        }

        binding.profileImage.setOnClickListener {
            showImagePickerDialog()
        }

        binding.btnEditProfile.setOnClickListener {
            enableEditMode()
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }

        loadStudentData()
    }

    override fun onResume() {
        super.onResume()
        loadStudentData()
    }

    // ===============================
    // IMAGE PICKER
    // ===============================
    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Profile Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun openCamera() {
        val file = File.createTempFile("profile_", ".jpg", cacheDir)

        cameraImageUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            file
        )

        cameraLauncher.launch(cameraImageUri)
    }

    // ===============================
    // LOAD DATA
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

                    binding.tvStudentName.text = name
                    binding.edtName.setText(name)
                    binding.edtEmail.setText(email)
                    binding.edtPhone.setText(phone)

                    val base64 = document.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        binding.profileImage.setImageBitmap(bitmap)
                        binding.header.ivProfile.setImageBitmap(bitmap)

                        val drawerImage = binding.studentDrawer.root
                            .findViewById<ImageView>(R.id.profileImage)

                        drawerImage.setImageBitmap(bitmap)
                    }
                }
            }
    }

    // ===============================
    // SAVE PROFILE
    // ===============================
    private fun saveProfileChanges() {

        val uid = auth.currentUser?.uid ?: return

        val updates = mapOf(
            "fullName" to binding.edtName.text.toString(),
            "email" to binding.edtEmail.text.toString(),
            "phone" to binding.edtPhone.text.toString()
        )

        db.collection("users")
            .document(uid)
            .update(updates)
            .addOnSuccessListener {
                binding.tvStudentName.text = binding.edtName.text.toString()
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            }
    }

    // ===============================
    // IMAGE SAVE (BASE64)
    // ===============================
    private fun convertAndSaveProfileImage(uri: Uri) {

        val uid = auth.currentUser?.uid ?: return

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)

            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            db.collection("users")
                .document(uid)
                .set(mapOf("profileImageBase64" to base64Image), SetOptions.merge())
                .addOnSuccessListener {

                    val bytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmapDecoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    binding.profileImage.setImageBitmap(bitmapDecoded)
                    binding.header.ivProfile.setImageBitmap(bitmapDecoded)

                    Toast.makeText(this, "Image Saved 🔥", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableEditMode() {
        binding.edtName.isEnabled = true
        binding.edtEmail.isEnabled = true
        binding.edtPhone.isEnabled = true

        binding.btnEditProfile.visibility = View.GONE
        binding.btnSaveProfile.visibility = View.VISIBLE
    }
}