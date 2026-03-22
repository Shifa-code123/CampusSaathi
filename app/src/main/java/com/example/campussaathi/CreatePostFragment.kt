package com.example.campussaathi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.Manifest
import android.content.pm.PackageManager
import okhttp3.MultipartBody

class CreatePostFragment : Fragment() {

    private lateinit var imgPreview: ImageView
    private lateinit var etHeading: EditText
    private lateinit var etCaption: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnChangeImage: ImageView

    private var selectedBitmap: Bitmap? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        selectedBitmap = bitmap
                        imgPreview.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(requireContext(), "Image load failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                selectedBitmap = bitmap
                imgPreview.setImageBitmap(bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)

        imgPreview = view.findViewById(R.id.imgPreview)
        etHeading = view.findViewById(R.id.etHeading)
        etCaption = view.findViewById(R.id.etCaption)
        btnUpload = view.findViewById(R.id.btnUpload)
        btnChangeImage = view.findViewById(R.id.btnChangeImage)

        imgPreview.setOnClickListener { showImageSourceDialog() }
        btnChangeImage.setOnClickListener { showImageSourceDialog() }
        btnUpload.setOnClickListener { uploadPost() }

        // Handle arguments (image passed from BusinessProfileFragment)
        arguments?.getByteArray("image_data")?.let { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            selectedBitmap = bitmap
            imgPreview.setImageBitmap(bitmap)
        }

        // Handle imageUri if passed via arguments (like the old Intent extra)
        arguments?.getString("imageUri")?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    imgPreview.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return view
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Upload from Gallery", "Open Camera")
        AlertDialog.Builder(requireContext())
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
            galleryLauncher.launch("image/*")
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 100)
        }
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap, callback: (String?) -> Unit) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()

        val url = "https://api.cloudinary.com/v1_1/drq2s3uzr/image/upload"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", byteArray.toRequestBody("image/*".toMediaType()))
            .addFormDataPart("upload_preset", "campussaathi_upload")
            .build()

        val request = Request.Builder().url(url).post(requestBody).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (responseBody == null) {
                    activity?.runOnUiThread { Toast.makeText(requireContext(), "Cloudinary response empty", Toast.LENGTH_LONG).show() }
                    return
                }
                try {
                    val json = JSONObject(responseBody)
                    if (json.has("secure_url")) {
                        val imageUrl = json.getString("secure_url")
                        activity?.runOnUiThread { callback(imageUrl) }
                    } else {
                        activity?.runOnUiThread { Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_LONG).show() }
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread { Toast.makeText(requireContext(), "Cloudinary error: ${e.message}", Toast.LENGTH_LONG).show() }
                }
            }
        })
    }

    private fun uploadPost() {
        val uid = auth.currentUser?.uid ?: return
        val heading = etHeading.text.toString().trim()
        val caption = etCaption.text.toString().trim()

        if (selectedBitmap == null) {
            Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }
        if (heading.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter heading", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show()

        uploadImageToCloudinary(selectedBitmap!!) { imageUrl ->
            if (imageUrl == null) {
                Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "Post uploaded", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}