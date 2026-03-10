package com.example.campussaathi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import android.util.Base64

class ActivityCityHelp : AppCompatActivity() {

    private lateinit var etServiceName: EditText
    private lateinit var etContact: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnGps: ImageView
    private lateinit var txtDistance: TextView
    private lateinit var btnAddPhoto: Button
    private lateinit var imgPreviewContainer: LinearLayout

    private val imageBitmapList = mutableListOf<Bitmap>()
    private val imageUrlList = mutableListOf<String>()

    private var selectedLatLng: LatLng? = null
    private var distanceKm: Double = 0.0

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val campusLatLng = LatLng(20.7074, 76.5680)

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val LOCATION_PERMISSION = 101

    // Gallery picker
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->

            uris.forEach { uri ->

                if (imageBitmapList.size >= 5) {
                    Toast.makeText(this, "Max 5 photos allowed", Toast.LENGTH_SHORT).show()
                    return@forEach
                }

                val stream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(stream)

                imageBitmapList.add(bitmap)
                addPreview(bitmap)
            }
        }

    // Camera picker
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->

            bitmap?.let {

                if (imageBitmapList.size >= 5) {
                    Toast.makeText(this, "Max 5 photos allowed", Toast.LENGTH_SHORT).show()
                    return@let
                }

                imageBitmapList.add(bitmap)
                addPreview(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_help)

        etServiceName = findViewById(R.id.etServiceName)
        etContact = findViewById(R.id.etContact)
        etDescription = findViewById(R.id.etDescription)

        btnSubmit = findViewById(R.id.btnSubmitService)
        btnGps = findViewById(R.id.btnGps)
        txtDistance = findViewById(R.id.txtDistance)

        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        imgPreviewContainer = findViewById(R.id.imgPreviewContainer)

        btnAddPhoto.setOnClickListener { showImageSourceDialog() }
        btnGps.setOnClickListener { getCurrentLocation() }
        btnSubmit.setOnClickListener { submitCityHelp() }

        setupDrawerAndFooter()
    }

    // ---------------- IMAGE SOURCE ----------------

    private fun showImageSourceDialog() {

        val options = arrayOf("Upload from Gallery", "Open Camera")

        AlertDialog.Builder(this)
            .setTitle("Select Option")
            .setItems(options) { _, which ->

                if (which == 0) openGallery()
                else openCamera()
            }
            .show()
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun addPreview(bitmap: Bitmap) {

        val img = ImageView(this)

        img.setImageBitmap(bitmap)

        img.layoutParams = LinearLayout.LayoutParams(200, 200)

        img.setPadding(8, 8, 8, 8)

        imgPreviewContainer.addView(img)
    }

    // ---------------- LOCATION ----------------

    private fun getCurrentLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION
            )

            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location != null) {

                    val lat = location.latitude
                    val lng = location.longitude

                    val latLng = LatLng(lat, lng)

                    selectedLatLng = latLng

                    calculateDistance(latLng)

                    Toast.makeText(this, "Location detected", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun calculateDistance(dest: LatLng) {

        val result = FloatArray(1)

        Location.distanceBetween(
            campusLatLng.latitude,
            campusLatLng.longitude,
            dest.latitude,
            dest.longitude,
            result
        )

        distanceKm = result[0] / 1000.0

        txtDistance.text = "Distance from campus: %.2f km".format(distanceKm)
    }

    // ---------------- CLOUDINARY ----------------

    private fun uploadImage(bitmap: Bitmap, onComplete: () -> Unit) {

        val stream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)

        val byteArray = stream.toByteArray()

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
            .url("https://api.cloudinary.com/v1_1/drq2s3uzr/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {}

            override fun onResponse(call: Call, response: Response) {

                val body = response.body?.string()

                val url = JSONObject(body).getString("secure_url")

                imageUrlList.add(url)

                if (imageUrlList.size == imageBitmapList.size) {

                    runOnUiThread { onComplete() }
                }
            }
        })
    }

    // ---------------- SUBMIT ----------------

    private fun submitCityHelp() {

        val name = etServiceName.text.toString()
        val contact = etContact.text.toString()
        val desc = etDescription.text.toString()

        val uid = auth.currentUser?.uid ?: return

        if (name.isEmpty() || contact.isEmpty()) {

            Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageBitmapList.isEmpty()) {

            Toast.makeText(this, "Add photos", Toast.LENGTH_SHORT).show()
            return
        }

        imageBitmapList.forEach {

            uploadImage(it) {

                val data = hashMapOf(

                    "ownerId" to uid,
                    "serviceName" to name,
                    "contact" to contact,
                    "description" to desc,
                    "photos" to imageUrlList,
                    "latitude" to selectedLatLng?.latitude,
                    "longitude" to selectedLatLng?.longitude,
                    "distanceFromCampus" to distanceKm,
                    "timestamp" to System.currentTimeMillis(),
                    "status" to "pending"
                )

                db.collection("cityhelp")
                    .add(data)
                    .addOnSuccessListener {

                        Toast.makeText(this, "City Help added", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }

    // ---------------- HEADER + FOOTER (UNCHANGED) ----------------

    private fun setupDrawerAndFooter() {

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_dashboard -> {
                    startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                    true
                }

                R.id.nav_services -> true

                R.id.nav_add -> {
                    startActivity(Intent(this, ActivityOwnerAddNewList1::class.java))
                    true
                }

                R.id.nav_reviews -> {
                    startActivity(Intent(this, OwnerReviewsActivity::class.java))
                    true
                }

                R.id.nav_performance -> {
                    startActivity(Intent(this, ActivityOwnerPerformance::class.java))
                    true
                }

                else -> false
            }
        }
    }
}