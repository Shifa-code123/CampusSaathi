package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

            val base64 = bitmapToBase64(bitmap)

            val intent = Intent(this, ActivityCreatePost::class.java)

            intent.putExtra("imageBase64", base64)

            startActivity(intent)

        }

    }

    // ================= BITMAP TO BASE64 =================
    private fun bitmapToBase64(bitmap: Bitmap): String {

        val outputStream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

    }
}