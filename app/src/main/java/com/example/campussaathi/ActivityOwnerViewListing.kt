package com.example.campussaathi

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.bumptech.glide.Glide


class ActivityOwnerViewListing : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private val db = FirebaseFirestore.getInstance()
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_view_listing)

        val headerProfile = findViewById<ImageView>(R.id.headerProfile)
        headerProfile.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_services

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

        val headerView = navigationView.inflateHeaderView(R.layout.owner_drawer_header)
        val headerName = headerView.findViewById<TextView>(R.id.headerName)
        val headerRole = headerView.findViewById<TextView>(R.id.headerRole)
        val headerProfileDrawer = headerView.findViewById<ImageView>(R.id.headerProfile)

        val uidDrawer = FirebaseAuth.getInstance().currentUser?.uid

        if (uidDrawer != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uidDrawer)
                .get()
                .addOnSuccessListener { doc ->

                    headerName.text = doc.getString("fullName") ?: "Owner"
                    headerRole.text = doc.getString("role") ?: "Owner"

                    val base64 = doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        headerProfileDrawer.setImageBitmap(bitmap)
                    }
                }
        }

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
                    // Already on my listing
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

        uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {

            db.collection("users")
                .document(uid!!)
                .get()
                .addOnSuccessListener { doc ->

                    val base64 = doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                        headerProfile.setImageBitmap(bitmap)

                    } else {

                        headerProfile.setImageResource(R.drawable.default_avatar)
                    }
                }
                .addOnFailureListener {
                    headerProfile.setImageResource(R.drawable.default_avatar)
                }
        }



        if (uid == null) {
            finish()
            return
        }

        loadListing()

        val btnDelete = findViewById<TextView>(R.id.btnDelete)

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadListing() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        FirebaseFirestore.getInstance()
            .collection("listings")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                val status = doc.getString("status") ?: "pending"

                txtStatus.text = status.replaceFirstChar { it.uppercase() }

                when (status) {
                    "pending" -> txtStatus.setBackgroundResource(R.drawable.status_pending)
                    "approved" -> txtStatus.setBackgroundResource(R.drawable.status_accepted)
                    "rejected" -> txtStatus.setBackgroundResource(R.drawable.status_rejected)
                }

            }

        db.collection("listings")
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                val ownerType = doc.getString("ownerType") ?: ""

                val txtBasicInfo = findViewById<TextView>(R.id.txtBasicInfo)
                val txtPricing = findViewById<TextView>(R.id.txtPricing)
                val txtAmenities = findViewById<TextView>(R.id.txtAmenities)
                val txtLocation = findViewById<TextView>(R.id.txtLocation)
                val txtCoordinates = findViewById<TextView>(R.id.txtCoordinates)
                val layoutImages = findViewById<LinearLayout>(R.id.layoutImages)

                when (ownerType) {

                    "room_pg" -> {
                        txtBasicInfo.text =
                            "Name: ${doc.getString("propertyName")}\n" +
                                    "Type: ${doc.getString("propertyType")}\n" +
                                    "Total Rooms: ${doc.getString("totalUnits")}\n" +
                                    "Vacant: ${doc.getString("availableUnits")}"

                        txtPricing.text =
                            "Rent: ₹${doc.getString("rent")} (${doc.getString("rentType")})"
                    }

                    "mess" -> {
                        txtBasicInfo.text =
                            "Mess: ${doc.getString("messName")}\n" +
                                    "Type: ${doc.getString("messType")}"

                        txtPricing.text =
                            "Monthly: ₹${doc.getString("monthlyCharge")}\n" +
                                    "Daily: ₹${doc.getString("dailyCharge")}"
                    }

                    "tuition" -> {
                        txtBasicInfo.text =
                            "Tuition: ${doc.getString("tuitionName")}\n" +
                                    "Type: ${doc.getString("tuitionType")}"

                        txtPricing.text =
                            "Fees: ₹${doc.getString("fees")}\n" +
                                    "Duration: ${doc.getString("duration")} days"
                    }
                }

                // Amenities
                val amenitiesList =
                    (doc.get("amenities") as? List<*>)?.joinToString(", ") ?: "-"
                txtAmenities.text = amenitiesList

                // Location
                txtLocation.text =
                    "${doc.getString("area")}\n" +
                            "Landmark: ${doc.getString("landmark")}\n" +
                            "City: ${doc.getString("city")}\n" +
                            "Pincode: ${doc.getString("pincode")}"

                txtCoordinates.text =
                    "Latitude: ${doc.getDouble("latitude")}\n" +
                            "Longitude: ${doc.getDouble("longitude")}"

                // Load Images (Base64)
                val images =
                    doc.get("imagesBase64") as? List<*>

                images?.forEach { base64 ->

                    val bytes = Base64.decode(base64 as String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val imageView = ImageView(this)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500
                    )
                    params.setMargins(0, 12, 0, 20)

                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setImageBitmap(bitmap)

                    layoutImages.addView(imageView)
                }
            }
    }

    // ✅ ADDED: Show confirmation before delete
    private fun showDeleteConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this listing? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteListing()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    // ✅ ADDED: Delete listing from Firestore
    private fun deleteListing() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("listings")
            .document(uid)
            .delete()
            .addOnSuccessListener {

                Toast.makeText(this, "Listing deleted", Toast.LENGTH_SHORT).show()

                // Navigate back to dashboard after delete
                startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete listing", Toast.LENGTH_SHORT).show()
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
