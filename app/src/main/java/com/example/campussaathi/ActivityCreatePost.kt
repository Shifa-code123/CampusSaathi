package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class ActivityCreatePost : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var etHeading: EditText
    private lateinit var etCaption: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnChangeImage: ImageView

    private var selectedImageBase64: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val PICK_IMAGE_REQUEST = 2001
    private val CAMERA_REQUEST = 2002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        imgPreview = findViewById(R.id.imgPreview)
        etHeading = findViewById(R.id.etHeading)
        etCaption = findViewById(R.id.etCaption)
        btnUpload = findViewById(R.id.btnUpload)
        btnChangeImage = findViewById(R.id.btnChangeImage)

        // If image came from previous screen
        val base64 = intent.getStringExtra("imageBase64")
        if (!base64.isNullOrEmpty()) {
            selectedImageBase64 = base64
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            imgPreview.setImageBitmap(bitmap)
        }

        imgPreview.setOnClickListener {
            showImageSourceDialog()
        }

        btnChangeImage.setOnClickListener {
            showImageSourceDialog()
        }

        btnUpload.setOnClickListener {
            uploadPost()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Upload from Gallery", "Open Camera")

        AlertDialog.Builder(this)
            .setTitle("Select Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        var bitmap: Bitmap? = null

        when (requestCode) {
            PICK_IMAGE_REQUEST -> {
                val uri = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }

            CAMERA_REQUEST -> {
                bitmap = data?.extras?.get("data") as Bitmap
            }
        }

        if (bitmap != null) {
            imgPreview.setImageBitmap(bitmap)
            selectedImageBase64 = bitmapToBase64(bitmap)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    private fun uploadPost() {

        val uid = auth.currentUser?.uid ?: return

        val heading = etHeading.text.toString().trim()
        val caption = etCaption.text.toString().trim()

        if (selectedImageBase64 == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (heading.isEmpty()) {
            Toast.makeText(this, "Please enter a heading", Toast.LENGTH_SHORT).show()
            return
        }

        val postData = hashMapOf(
            "ownerId" to uid,
            "heading" to heading,
            "caption" to caption,
            "imageBase64" to selectedImageBase64,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("posts")
            .add(postData)
            .addOnSuccessListener {
                Toast.makeText(this, "Post uploaded", Toast.LENGTH_SHORT).show()
                finish() // return to profile
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }
}