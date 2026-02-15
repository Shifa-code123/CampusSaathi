package com.example.campussaathi

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class ActivityOwnerAddNewList5 : AppCompatActivity() {

    private lateinit var gridImages: GridLayout
    private lateinit var btnSubmit: Button
    private lateinit var btnAddImage: LinearLayout

    private val imageUris = mutableListOf<Uri>()
    private var listingId: String? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                if (imageUris.size < 5) {
                    imageUris.add(it)
                    addImageToGrid(it)
                } else {
                    Toast.makeText(this, "Maximum 5 images allowed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list5)

        listingId = intent.getStringExtra("LISTING_ID")

        if (listingId == null) {
            Toast.makeText(this, "Listing ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        gridImages = findViewById(R.id.gridImages)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnAddImage = findViewById(R.id.btnAddImage)

        btnAddImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            uploadImagesToFirebase()
        }
    }

    private fun addImageToGrid(uri: Uri) {

        val imageView = ImageView(this)

        val size = resources.displayMetrics.widthPixels / 3 - 40

        val params = GridLayout.LayoutParams()
        params.width = size
        params.height = size
        params.setMargins(12, 12, 12, 12)

        imageView.layoutParams = params
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        Glide.with(this).load(uri).into(imageView)

        gridImages.addView(imageView)
    }

    private fun uploadImagesToFirebase() {

        if (imageUris.size < 3) {
            Toast.makeText(this, "Upload at least 3 photos", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        val storageRef = FirebaseStorage.getInstance().reference
        val uploadedUrls = mutableListOf<String>()

        imageUris.forEachIndexed { index, uri ->

            val ref = storageRef.child("listings/$listingId/$index.jpg")

            ref.putFile(uri)
                .addOnSuccessListener {

                    ref.downloadUrl.addOnSuccessListener { downloadUri ->

                        uploadedUrls.add(downloadUri.toString())

                        if (uploadedUrls.size == imageUris.size) {
                            saveListing(uploadedUrls)
                        }
                    }
                }
                .addOnFailureListener {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveListing(images: List<String>) {

        val data = hashMapOf(
            "images" to images,
            "ownerId" to FirebaseAuth.getInstance().currentUser?.uid,
            "status" to "active"
        )

        FirebaseFirestore.getInstance()
            .collection("listings")
            .document(listingId!!)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {

                Toast.makeText(this, "Listing submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                btnSubmit.isEnabled = true
                Toast.makeText(this, "Failed to save listing", Toast.LENGTH_SHORT).show()
            }
    }
}
