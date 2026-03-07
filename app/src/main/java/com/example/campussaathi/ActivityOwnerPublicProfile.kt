package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.io.ByteArrayOutputStream

class ActivityOwnerPublicProfile : AppCompatActivity() {

    // Profile Views
    private lateinit var profileImage: ImageView
    private lateinit var headerProfile: ImageView
    private lateinit var txtPostsCount: TextView
    private lateinit var txtFollowersCount: TextView
    private lateinit var txtOwnerName: TextView
    private lateinit var txtBio: TextView
    private lateinit var fabAddPost: FloatingActionButton


    private lateinit var navigationView: NavigationView

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var toggle: ActionBarDrawerToggle

    // Tabs
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private lateinit var btnEditProfile: Button

    // Firebase
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val PICK_IMAGE_REQUEST = 1001
    private val CAMERA_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_public_profile)

        btnEditProfile = findViewById(R.id.btnEditProfile)

        initViews()
        loadProfileData()
        setupTabs()

        fabAddPost.setOnClickListener {
            showImageSourceDialog()
        }

        btnEditProfile.setOnClickListener {

            val intent = Intent(this, ActivityOwnerProfile::class.java)
            startActivity(intent)

        }

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


        val uid = FirebaseAuth.getInstance().currentUser?.uid

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

    // ================= INITIALIZE VIEWS =================
    private fun initViews() {

        profileImage = findViewById(R.id.profileImage)
        headerProfile = findViewById(R.id.headerProfile)
        txtPostsCount = findViewById(R.id.txtPostsCount)
        txtFollowersCount = findViewById(R.id.txtFollowersCount)
        txtOwnerName = findViewById(R.id.txtOwnerName)
        txtBio = findViewById(R.id.txtBio)
        fabAddPost = findViewById(R.id.fabAddPost)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

    }

    // ================= SETUP TABS =================
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

    // ================= LOAD PROFILE DATA =================
    private fun loadProfileData() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                txtBio.text = doc.getString("bio") ?: "Add your bio..."

                val base64 = doc.getString("profileImageBase64")

                if (!base64.isNullOrEmpty()) {

                    val bytes = Base64.decode(base64, Base64.DEFAULT)

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    profileImage.setImageBitmap(bitmap)
                    headerProfile.setImageBitmap(bitmap)
                }

                loadPostsCount(uid)
            }
    }

    // ================= LOAD POSTS COUNT =================
    private fun loadPostsCount(uid: String) {

        db.collection("posts")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { documents ->

                txtPostsCount.text = documents.size().toString()

            }
    }

    // ================= IMAGE SOURCE DIALOG =================
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

    // ================= HANDLE IMAGE RESULT =================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        var imageUri: Uri? = null
        var bitmap: Bitmap? = null

        when (requestCode) {

            PICK_IMAGE_REQUEST -> {

                imageUri = data?.data

            }

            CAMERA_REQUEST -> {

                bitmap = data?.extras?.get("data") as Bitmap

            }

        }

        val intent = Intent(this, ActivityCreatePost::class.java)

        if (imageUri != null) {

            intent.putExtra("imageUri", imageUri.toString())

        } else if (bitmap != null) {

            val uri = Uri.parse(MediaStore.Images.Media.insertImage(
                contentResolver,
                bitmap,
                "temp",
                null
            ))

            intent.putExtra("imageUri", uri.toString())
        }

        startActivity(intent)

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