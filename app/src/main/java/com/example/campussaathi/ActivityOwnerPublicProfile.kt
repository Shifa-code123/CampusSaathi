package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL

class ActivityOwnerPublicProfile : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var headerProfile: ImageView
    private lateinit var txtPostsCount: TextView
    private lateinit var txtFollowersCount: TextView
    private lateinit var txtOwnerName: TextView
    private lateinit var txtBio: TextView
    private lateinit var fabAddPost: FloatingActionButton
    private lateinit var btnEditProfile: Button

    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val PICK_IMAGE_REQUEST = 1001
    private val CAMERA_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_public_profile)

        initViews()
        setupTabs()
        loadProfileData()

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }

        profileImage.setOnClickListener {
            showImageSourceDialog()
        }

        fabAddPost.setOnClickListener {
            showImageSourceDialog()
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

        setupDrawerNavigation()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_dashboard -> {
                    startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                    true
                }

                R.id.nav_services -> true

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
    }

    // ================= INIT VIEWS =================

    private fun initViews() {

        profileImage = findViewById(R.id.profileImage)
        headerProfile = findViewById(R.id.headerProfile)

        txtPostsCount = findViewById(R.id.txtPostsCount)
        txtFollowersCount = findViewById(R.id.txtFollowersCount)
        txtOwnerName = findViewById(R.id.txtOwnerName)
        txtBio = findViewById(R.id.txtBio)

        fabAddPost = findViewById(R.id.fabAddPost)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
    }

    // ================= LOAD PROFILE =================

    private fun loadProfileData() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                txtBio.text = doc.getString("bio") ?: "Add your bio..."

                loadBusinessProfileImage(uid)
                loadPostsCount(uid)
            }
    }

    // ================= LOAD CLOUDINARY IMAGE =================

    private fun loadBusinessProfileImage(uid: String) {

        db.collection("posts")
            .document(uid)   // directly read document by UID
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val url = doc.getString("business_pic")

                    if (!url.isNullOrEmpty()) {

                        Thread {

                            try {

                                val bitmap =
                                    BitmapFactory.decodeStream(URL(url).openStream())

                                runOnUiThread {

                                    profileImage.setImageBitmap(bitmap)
                                    headerProfile.setImageBitmap(bitmap)

                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }.start()
                    }
                }
            }
    }

    // ================= POSTS COUNT =================

    private fun loadPostsCount(uid: String) {

        db.collection("posts")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener {

                txtPostsCount.text = it.size().toString()
            }
    }

    // ================= IMAGE SOURCE =================

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

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(intent, CAMERA_REQUEST)
    }

    // ================= IMAGE RESULT =================

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        var bitmap: Bitmap? = null

        when (requestCode) {

            PICK_IMAGE_REQUEST -> {

                val uri = data?.data

                val stream = contentResolver.openInputStream(uri!!)

                bitmap = BitmapFactory.decodeStream(stream)
            }

            CAMERA_REQUEST -> {

                bitmap = data?.extras?.get("data") as Bitmap
            }
        }

        bitmap?.let {

            profileImage.setImageBitmap(it)

            uploadImageToCloudinary(it)
        }
    }

    // ================= CLOUDINARY UPLOAD =================

    private fun uploadImageToCloudinary(bitmap: Bitmap) {

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

                saveBusinessPic(url)
            }
        })
    }

    // ================= SAVE URL =================

    private fun saveBusinessPic(url: String) {

        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(

            "ownerId" to uid,
            "business_pic" to url
        )

        db.collection("posts")
            .document(uid)
            .set(data)
            .addOnSuccessListener {

                runOnUiThread {

                    Toast.makeText(
                        this,
                        "Profile picture updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    // ================= TABS =================

    private fun setupTabs() {

        val adapter = ProfilePagerAdapter(this)

        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

            when (position) {

                0 -> tab.setIcon(R.drawable.ic_grid)
                1 -> tab.setIcon(R.drawable.ic_services)
            }

        }.attach()
    }

    // ================= DRAWER =================

    private fun setupDrawerNavigation() {

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_add_listing -> openOrResumeListing()

                R.id.nav_submission ->
                    startActivity(Intent(this, ActivityOwnerEditListing::class.java))

                R.id.nav_my_listing ->
                    startActivity(Intent(this, ActivityOwnerViewListing::class.java))

                R.id.nav_requests ->
                    startActivity(Intent(this, OwnerReviewsActivity::class.java))

                R.id.nav_notifications ->
                    startActivity(Intent(this, ActivityOwnerNotification::class.java))

                R.id.nav_profile ->
                    startActivity(Intent(this, ActivityOwnerProfile::class.java))

                R.id.nav_logout ->
                    logoutUser()
            }

            drawerLayout.closeDrawer(GravityCompat.START)

            true
        }
    }

    // ================= LISTING =================

    private fun openOrResumeListing() {

        startActivity(Intent(this, ActivityOwnerAddNewList1::class.java))
    }

    // ================= LOGOUT =================

    private fun logoutUser() {

        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)

        finish()
    }
}