package com.example.campussaathi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class ActivityCreatePost : AppCompatActivity() {

    private lateinit var imgPreview: ImageView
    private lateinit var etHeading: EditText
    private lateinit var etCaption: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnChangeImage: ImageView

    private var selectedBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // -------- Gallery Picker --------
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->

            if (uri != null) {

                try {

                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap != null) {

                        selectedBitmap = bitmap
                        imgPreview.setImageBitmap(bitmap)

                    } else {

                        Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    // -------- Camera Picker --------
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->

            if (bitmap != null) {

                selectedBitmap = bitmap
                imgPreview.setImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        imgPreview = findViewById(R.id.imgPreview)
        etHeading = findViewById(R.id.etHeading)
        etCaption = findViewById(R.id.etCaption)
        btnUpload = findViewById(R.id.btnUpload)
        btnChangeImage = findViewById(R.id.btnChangeImage)

        imgPreview.setOnClickListener {
            showImageSourceDialog()
        }

        btnChangeImage.setOnClickListener {
            showImageSourceDialog()
        }

        btnUpload.setOnClickListener {
            uploadPost()
        }

        // Permission check
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                100
            )
        }

        // 🔥 Receive imageUri from previous Activity
        val imageUriString = intent.getStringExtra("imageUri")

        if (!imageUriString.isNullOrEmpty()) {

            try {

                val uri = Uri.parse(imageUriString)

                val inputStream = contentResolver.openInputStream(uri)

                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap != null) {

                    selectedBitmap = bitmap
                    imgPreview.setImageBitmap(bitmap)

                } else {

                    Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {

                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

            } else {

                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // -------- Select Image Source --------
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

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            galleryLauncher.launch("image/*")

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                100
            )
        }
    }

    private fun openCamera() {

        cameraLauncher.launch(null)
    }

    // -------- Upload Image to Cloudinary --------
    private fun uploadImageToCloudinary(bitmap: Bitmap, callback: (String?) -> Unit) {

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        val byteArray = outputStream.toByteArray()

        val url = "https://api.cloudinary.com/v1_1/drq2s3uzr/image/upload"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                byteArray.toRequestBody("image/*".toMediaType())
            )
            .addFormDataPart("upload_preset", "campussaathi_upload")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {

                runOnUiThread {

                    Toast.makeText(
                        this@ActivityCreatePost,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {

                val responseBody = response.body?.string()

                if (responseBody == null) {

                    runOnUiThread {
                        Toast.makeText(
                            this@ActivityCreatePost,
                            "Cloudinary response empty",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return
                }

                try {

                    val json = JSONObject(responseBody)

                    if (json.has("secure_url")) {

                        val imageUrl = json.getString("secure_url")

                        runOnUiThread {
                            callback(imageUrl)
                        }

                    } else {

                        runOnUiThread {

                            Toast.makeText(
                                this@ActivityCreatePost,
                                "Image upload failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                } catch (e: Exception) {

                    runOnUiThread {

                        Toast.makeText(
                            this@ActivityCreatePost,
                            "Cloudinary error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    // -------- Upload Post --------
    private fun uploadPost() {

        val uid = auth.currentUser?.uid ?: return

        val heading = etHeading.text.toString().trim()
        val caption = etCaption.text.toString().trim()

        if (selectedBitmap == null) {

            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        if (heading.isEmpty()) {

            Toast.makeText(this, "Please enter heading", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

        uploadImageToCloudinary(selectedBitmap!!) { imageUrl ->

            if (imageUrl == null) {

                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                return@uploadImageToCloudinary
            }

            val postData = hashMapOf(

                "ownerId" to uid,
                "heading" to heading,
                "caption" to caption,
                "img" to imageUrl,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("posts")
                .add(postData)
                .addOnSuccessListener {

                    Toast.makeText(this, "Post uploaded", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {

                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}