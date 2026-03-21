package com.example.campussaathi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class EditServiceFormActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etContact: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var imgContainer: LinearLayout
    private lateinit var btnAddPhoto: Button
    private lateinit var btnGps: Button
    private lateinit var btnUpdate: Button

    private var serviceId: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val existingPhotos = mutableListOf<String>()
    private val newPhotosBitmaps = mutableListOf<Bitmap>()
    private var selectedLatLng: LatLng? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri ->
            val stream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream)
            newPhotosBitmaps.add(bitmap)
            addBitmapPreview(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service_form)

        serviceId = intent.getStringExtra("SERVICE_ID")

        etName = findViewById(R.id.etEditServiceName)
        etContact = findViewById(R.id.etEditContact)
        etDescription = findViewById(R.id.etEditDescription)
        spinnerCategory = findViewById(R.id.spinnerEditCategory)
        imgContainer = findViewById(R.id.editImgPreviewContainer)
        btnAddPhoto = findViewById(R.id.btnAddMorePhoto)
        btnGps = findViewById(R.id.btnEditGps)
        btnUpdate = findViewById(R.id.btnUpdateService)

        loadServiceData()

        btnAddPhoto.setOnClickListener { galleryLauncher.launch("image/*") }
        btnGps.setOnClickListener { getCurrentLocation() }
        btnUpdate.setOnClickListener { updateService() }
    }

    private fun loadServiceData() {
        if (serviceId == null) return

        db.collection("services").document(serviceId!!)
            .get()
            .addOnSuccessListener { doc ->
                etName.setText(doc.getString("serviceName"))
                etContact.setText(doc.getString("contact"))
                etDescription.setText(doc.getString("description"))
                
                val category = doc.getString("category")
                val categories = resources.getStringArray(R.array.service_categories)
                val index = categories.indexOf(category)
                if (index != -1) spinnerCategory.setSelection(index)

                val photos = doc.get("photos") as? List<String> ?: emptyList()
                existingPhotos.addAll(photos)
                photos.forEach { addUrlPreview(it) }

                val lat = doc.getDouble("latitude")
                val lng = doc.getDouble("longitude")
                if (lat != null && lng != null) {
                    selectedLatLng = LatLng(lat, lng)
                }
            }
    }

    private fun addUrlPreview(url: String) {
        val img = ImageView(this)
        img.layoutParams = LinearLayout.LayoutParams(250, 250).apply { setMargins(8, 0, 8, 0) }
        img.scaleType = ImageView.ScaleType.CENTER_CROP
        Glide.with(this).load(url).into(img)
        imgContainer.addView(img)
    }

    private fun addBitmapPreview(bitmap: Bitmap) {
        val img = ImageView(this)
        img.layoutParams = LinearLayout.LayoutParams(250, 250).apply { setMargins(8, 0, 8, 0) }
        img.scaleType = ImageView.ScaleType.CENTER_CROP
        img.setImageBitmap(bitmap)
        imgContainer.addView(img)
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { loc ->
            loc?.let {
                selectedLatLng = LatLng(it.latitude, it.longitude)
                Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateService() {
        if (newPhotosBitmaps.isNotEmpty()) {
            uploadNewImages { urls ->
                val finalPhotos = existingPhotos + urls
                saveToFirestore(finalPhotos)
            }
        } else {
            saveToFirestore(existingPhotos)
        }
    }

    private fun uploadNewImages(onComplete: (List<String>) -> Unit) {
        val uploadedUrls = mutableListOf<String>()
        var count = 0
        newPhotosBitmaps.forEach { bitmap ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", "image.jpg", stream.toByteArray().toRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", "campussaathi_upload")
                .build()

            val request = Request.Builder().url("https://api.cloudinary.com/v1_1/drq2s3uzr/image/upload").post(body).build()
            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) {
                    val url = JSONObject(response.body?.string() ?: "").getString("secure_url")
                    uploadedUrls.add(url)
                    count++
                    if (count == newPhotosBitmaps.size) runOnUiThread { onComplete(uploadedUrls) }
                }
            })
        }
    }

    private fun saveToFirestore(photoList: List<String>) {
        val data = hashMapOf(
            "serviceName" to etName.text.toString(),
            "contact" to etContact.text.toString(),
            "description" to etDescription.text.toString(),
            "category" to spinnerCategory.selectedItem.toString(),
            "photos" to photoList,
            "latitude" to selectedLatLng?.latitude,
            "longitude" to selectedLatLng?.longitude,
            "status" to "pending"
        )

        db.collection("services").document(serviceId!!)
            .update(data as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Service Updated & Sent for Approval", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ActivityOwnerSubmissionList1::class.java))
                finish()
            }
    }
}