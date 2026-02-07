package com.example.campussaathi

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ActivityOwnerAddNewList5 : AppCompatActivity() {

    private lateinit var gridImages: GridLayout
    private val imageUris = mutableListOf<Uri>()

    // Image picker (modern & correct)
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                if (imageUris.size < 5) {
                    imageUris.add(it)
                    addImageToGrid(it)
                } else {
                    Toast.makeText(this, "Max 5 images allowed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list5)

        // Bind views
        gridImages = findViewById(R.id.gridImages)

        // Add photo click (LinearLayout)
        findViewById<LinearLayout>(R.id.btnAddImage).setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Submit button click
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            uploadImagesToFirebase()
        }
    }

    // Add image preview to GridLayout
    private fun addImageToGrid(uri: Uri) {
        val imageView = ImageView(this)

        val params = GridLayout.LayoutParams()
        params.width = 220
        params.height = 220
        params.setMargins(12, 12, 12, 12)
        imageView.layoutParams = params

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this).load(uri).into(imageView)

        gridImages.addView(imageView)
    }

    // Upload images to Firebase Storage
    private fun uploadImagesToFirebase() {
        if (imageUris.size < 3) {
            Toast.makeText(this, "Upload at least 3 photos", Toast.LENGTH_SHORT).show()
            return
        }

        val listingId = FirebaseFirestore.getInstance()
            .collection("listings")
            .document()
            .id

        val storageRef = FirebaseStorage.getInstance().reference
        val uploadedUrls = mutableListOf<String>()

        imageUris.forEachIndexed { index, uri ->
            val ref = storageRef.child("listings/$listingId/$index.jpg")

            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { downloadUri ->
                        uploadedUrls.add(downloadUri.toString())

                        if (uploadedUrls.size == imageUris.size) {
                            saveListing(listingId, uploadedUrls)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Save listing data in Firestore
    private fun saveListing(listingId: String, images: List<String>) {
        val data = hashMapOf(
            "images" to images,
            "ownerId" to FirebaseAuth.getInstance().currentUser?.uid
        )

        FirebaseFirestore.getInstance()
            .collection("listings")
            .document(listingId)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Listing submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save listing", Toast.LENGTH_SHORT).show()
            }
    }
}
