package com.example.campussaathi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout


class AddServicesActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_add_services)

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

        btnSubmit.setOnClickListener { submitService() }


// drawer and footer code start here

        val headerProfile = findViewById<ImageView>(R.id.headerProfile)
        headerProfile.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }



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

        // Toolbar profile image
        val headerProfileToolbar =
            findViewById<ImageView>(R.id.headerProfile)

// Drawer header
        val headerView = navigationView.getHeaderView(0)

        val headerProfileDrawer =
            headerView.findViewById<ImageView>(R.id.headerProfile)

        val headerName =
            headerView.findViewById<TextView>(R.id.headerName)

        val headerRole =
            headerView.findViewById<TextView>(R.id.headerRole)

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    headerName.text =
                        doc.getString("fullName") ?: "Owner"

                    headerRole.text =
                        doc.getString("role") ?: "Service Owner"

                    val base64 =
                        doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes =
                            Base64.decode(base64, Base64.DEFAULT)

                        val bitmap =
                            BitmapFactory.decodeByteArray(bytes,0,bytes.size)

                        // set both images
                        headerProfileToolbar.setImageBitmap(bitmap)
                        headerProfileDrawer.setImageBitmap(bitmap)
                    }
                }
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_services


        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_add_listing -> openOrResumeListing()

                R.id.nav_submission -> {
                    startActivity(Intent(this, ActivityOwnerEditListing::class.java))
                }

                R.id.nav_my_listing -> {
                    startActivity(Intent(this, ActivityOwnerViewListing::class.java))
                }

                R.id.nav_requests -> {
                    startActivity(Intent(this, OwnerReviewsActivity::class.java))
                }

                R.id.nav_notifications -> {
                    startActivity(Intent(this, ActivityOwnerNotification::class.java))
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ActivityOwnerProfile::class.java))
                }

                R.id.nav_logout -> logoutUser()
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard -> {
                    startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                    true
                }

                R.id.nav_services -> {
                    true
                }

                R.id.nav_add -> {
                    openOrResumeListing()
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




        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    val base64 = doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        headerProfile.setImageBitmap(bitmap)
                    }
                }
        }

    }



    // IMAGE SOURCE
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

    // IMAGE PREVIEW
    private fun addPreview(bitmap: Bitmap) {

        val img = ImageView(this)

        img.setImageBitmap(bitmap)

        img.layoutParams = LinearLayout.LayoutParams(200,200)

        img.setPadding(8,8,8,8)

        imgPreviewContainer.addView(img)
    }

    // LOCATION
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

                    // OPEN GOOGLE MAPS
                    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(Service Location)")
                    val intent = Intent(Intent.ACTION_VIEW, uri)

                    intent.setPackage("com.google.android.apps.maps")

                    startActivity(intent)

                } else {

                    Toast.makeText(this, "Unable to detect location", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            getCurrentLocation()
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

    // CLOUDINARY
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

    // SUBMIT
    private fun submitService() {

        val name = etServiceName.text.toString()
        val contact = etContact.text.toString()
        val desc = etDescription.text.toString()

        val uid = auth.currentUser?.uid ?: return

        if (name.isEmpty() || contact.isEmpty()) {

            Toast.makeText(this,"Fill required fields",Toast.LENGTH_SHORT).show()
            return
        }

        if (imageBitmapList.isEmpty()) {

            Toast.makeText(this,"Add photos",Toast.LENGTH_SHORT).show()
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

                    // NEW FIELD
                    "status" to "pending"
                )

                db.collection("services")
                    .add(data)
                    .addOnSuccessListener {

                        Toast.makeText(this,"Service added",Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }

    private fun openOrResumeListing() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("owner_verifications")
            .document(uid)
            .get()
            .addOnSuccessListener { ownerDoc ->

                val ownerType = ownerDoc.getString("ownerType")?.lowercase()

                if (ownerType == null) {
                    Toast.makeText(this, "Owner type missing", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("listings")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { listingDoc ->

                        if (!listingDoc.exists()) {

                            val data = hashMapOf(
                                "ownerId" to uid,
                                "ownerType" to ownerType,
                                "status" to "draft",
                                "currentStep" to 1
                            )

                            db.collection("listings")
                                .document(uid)
                                .set(data)
                                .addOnSuccessListener {

                                    startActivity(
                                        Intent(this, ActivityOwnerAddNewList1::class.java)
                                            .putExtra("OWNER_TYPE", ownerType)
                                    )
                                }

                        } else {

                            val status = listingDoc.getString("status")
                            val currentStep =
                                listingDoc.getLong("currentStep")?.toInt() ?: 1

                            // If listing already submitted → open submission screen
                            if (status == "pending") {
                                startActivity(
                                    Intent(this, ActivityOwnerSubmissionList1::class.java)
                                )
                                return@addOnSuccessListener
                            }

                            // If all 6 steps completed → open submission screen
                            if (currentStep >= 6) {
                                startActivity(
                                    Intent(this, ActivityOwnerSubmissionList1::class.java)
                                )
                                return@addOnSuccessListener
                            }

                            when (currentStep) {

                                1 -> startActivity(
                                    Intent(this, ActivityOwnerAddNewList1::class.java)
                                        .putExtra("OWNER_TYPE", ownerType)
                                )

                                2 -> startActivity(Intent(this, ActivityOwnerAddNewList2::class.java))

                                3 -> startActivity(Intent(this, ActivityOwnerAddNewList3::class.java))

                                4 -> startActivity(Intent(this, ActivityOwnerAddNewList4::class.java))

                                5 -> startActivity(Intent(this, ActivityOwnerAddNewList5::class.java))

                                6 -> startActivity(Intent(this, ActivityOwnerAddNewList6::class.java))
                            }
                        }
                    }
            }
    }


    private fun logoutUser() {

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }




}