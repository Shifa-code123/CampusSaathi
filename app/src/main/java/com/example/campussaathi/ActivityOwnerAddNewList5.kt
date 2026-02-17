package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream

class ActivityOwnerAddNewList5 : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var gridImages: GridLayout
    private lateinit var btnSubmit: Button
    private lateinit var btnAddImage: LinearLayout

    private val imageBase64List = mutableListOf<String>()
    private val db = FirebaseFirestore.getInstance()

    private var uid: String? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { convertToBase64(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list5)

        uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupDrawer()
        initViews()

        btnAddImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            saveImagesToFirestore()
        }
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open, R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun initViews() {
        gridImages = findViewById(R.id.gridImages)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnAddImage = findViewById(R.id.btnAddImage)
    }

    // 🔥 Convert image to Base64 (like profile logic)
    private fun convertToBase64(uri: Uri) {

        if (imageBase64List.size >= 10) {
            Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 35, outputStream)

            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            imageBase64List.add(base64Image)

            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)
            imageView.layoutParams =
                GridLayout.LayoutParams().apply {
                    width = resources.displayMetrics.widthPixels / 3 - 40
                    height = width
                    setMargins(12, 12, 12, 12)
                }

            gridImages.addView(imageView)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImagesToFirestore() {

        if (imageBase64List.size < 5) {
            Toast.makeText(this, "Upload minimum 5 photos", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "imagesBase64" to imageBase64List,
            "currentStep" to 6,
            "status" to "draft"
        )

        db.collection("listings")
            .document(uid!!)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Step 5 saved", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ActivityOwnerAddNewList6::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
            }
    }
}
