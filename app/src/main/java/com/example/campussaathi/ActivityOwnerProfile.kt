package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import androidx.appcompat.app.AlertDialog


class ActivityOwnerProfile : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtRole: TextView
    private lateinit var txtPhone: TextView
    private lateinit var txtEmail: TextView
    private lateinit var txtAddress: TextView

    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var layoutVerified: LinearLayout

    private lateinit var txtBio: TextView
    private lateinit var itemEdit: LinearLayout

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var isEditMode = false

    // 🔵 Image Picker
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { convertAndSaveProfileImage(it) }
        }


    // ===== Drawer Variables Start =====
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var navigationView: com.google.android.material.navigation.NavigationView
    private lateinit var toggle: androidx.appcompat.app.ActionBarDrawerToggle
    // ===== Drawer Variables End =====



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_profile)

        // ===== Drawer Setup Start =====

        // Initialize Drawer & Toolbar
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_view)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val headerView = navigationView.inflateHeaderView(R.layout.owner_drawer_header)

        val headerName = headerView.findViewById<TextView>(R.id.headerName)
        val headerRole = headerView.findViewById<TextView>(R.id.headerRole)
        val headerProfile = headerView.findViewById<ImageView>(R.id.headerProfile)
        txtBio = findViewById(R.id.txtbio)
        itemEdit = findViewById(R.id.itemEdit)

        // Make Toolbar act as ActionBar
        setSupportActionBar(toolbar)

        // Connect Drawer with Toolbar (Hamburger logic)
        toggle = androidx.appcompat.app.ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )

        // Attach toggle listener
        drawerLayout.addDrawerListener(toggle)

        // Sync hamburger icon state
        toggle.syncState()

// ADD THIS HERE ↓↓↓


        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    headerName.text = doc.getString("fullName") ?: "Owner"
                    headerRole.text = doc.getString("role") ?: "Owner"

                    val base64 = doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        headerProfile.setImageBitmap(bitmap)
                    }
                }
        }

        bindViews()
        loadProfileData()

//        itemEdit.setOnClickListener {
//            if (!isEditMode) enableEditMode() else saveProfileChanges()
//        }

        imgProfile.setOnClickListener {
            imagePicker.launch("image/*")
        }


        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_add_listing -> {
                    // Already in this screen
                }

                R.id.nav_submission -> {
                    startActivity(Intent(this, ActivityOwnerSubmissionList1::class.java))
                }

//                R.id.nav_my_listing -> {
//                    startActivity(Intent(this, ActivityOwnerMyList::class.java))
//                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ActivityOwnerProfile::class.java))
                }

                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }

            // Close drawer after click
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START)
            true
        }

        // ===== Drawer Setup End =====


        itemEdit.setOnClickListener {

            if (!isEditMode) enableEditMode() else saveProfileChanges()

            val input = EditText(this)
            input.setText(txtBio.text.toString())

            AlertDialog.Builder(this)
                .setTitle("Edit Bio")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->

                    val newBio = input.text.toString()

                    val uid = auth.currentUser?.uid ?: return@setPositiveButton

                    db.collection("users")
                        .document(uid)
                        .update("bio", newBio)
                        .addOnSuccessListener {

                            txtBio.text = newBio

                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }





    }

    private fun bindViews() {
        imgProfile = findViewById(R.id.imgProfile)
        txtName = findViewById(R.id.txtName)
        txtRole = findViewById(R.id.txtRole)
        txtPhone = findViewById(R.id.txtPhone)
        txtEmail = findViewById(R.id.txtEmail)
        txtAddress = findViewById(R.id.txtAddress)

        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)

        layoutVerified = findViewById(R.id.layoutVerified)
        itemEdit = findViewById(R.id.itemEdit)
    }

    // 🔵 LOAD PROFILE
    private fun loadProfileData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                val name = doc.getString("fullName")
                txtName.text = if (!name.isNullOrEmpty()) name else "Owner"
                txtRole.text = doc.getString("role")
                txtPhone.text = doc.getString("phone")
                txtEmail.text = doc.getString("email")
                txtAddress.text = doc.getString("location")

                layoutVerified.visibility =
                    if (doc.getBoolean("verified") == true) View.VISIBLE else View.GONE

                try {
                    val base64 = doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imgProfile.setImageBitmap(bitmap)
                    } else {
                        imgProfile.setImageResource(R.drawable.default_avatar)
                    }

                } catch (e: Exception) {
                    imgProfile.setImageResource(R.drawable.default_avatar)
                }

            }
    }

    private fun enableEditMode() {
        isEditMode = true

        edtPhone.setText(txtPhone.text)
        edtAddress.setText(txtAddress.text)

        txtPhone.visibility = View.GONE
        txtAddress.visibility = View.GONE

        edtPhone.visibility = View.VISIBLE
        edtAddress.visibility = View.VISIBLE

        Toast.makeText(this, "Edit mode enabled", Toast.LENGTH_SHORT).show()
    }

    private fun saveProfileChanges() {
        val uid = auth.currentUser?.uid ?: return

        val updateMap = mapOf(
            "phone" to edtPhone.text.toString().trim(),
            "location" to edtAddress.text.toString().trim()
        )

        db.collection("users").document(uid)
            .update(updateMap)
            .addOnSuccessListener {

                txtPhone.text = edtPhone.text
                txtAddress.text = edtAddress.text

                txtPhone.visibility = View.VISIBLE
                txtAddress.visibility = View.VISIBLE

                edtPhone.visibility = View.GONE
                edtAddress.visibility = View.GONE

                isEditMode = false
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔥 Convert profile image to Base64 and save
    private fun convertAndSaveProfileImage(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(
                android.graphics.Bitmap.CompressFormat.JPEG,
                35,
                outputStream
            )

            val byteArray = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)

            db.collection("users").document(uid)
                .update("profileImageBase64", base64Image)
                .addOnSuccessListener {

                    val bytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmapDecoded =
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    imgProfile.setImageBitmap(bitmapDecoded)

                    Toast.makeText(this, "Profile image updated", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBio() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val bio = doc.getString("bio")

                if (!bio.isNullOrEmpty()) {
                    txtBio.text = bio
                } else {
                    txtBio.text = "Add bio"
                }

            }
    }
}