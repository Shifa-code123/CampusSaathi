package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerPerformance : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var headerProfileToolbar: ImageView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_performance)

        // TOOLBAR
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        headerProfileToolbar = findViewById(R.id.headerProfile)

        headerProfileToolbar.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }

        loadToolbarProfile()

        // DRAWER
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_view)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )
        loadDrawerHeader()
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard ->
                    startActivity(Intent(this, ActivityOwnerDashboard::class.java))

                R.id.nav_add_listing ->
                    openOrResumeListing()

                R.id.nav_submission ->
                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerEditListing::class.java
                        )
                    )
                R.id.nav_my_listing ->
                    startActivity(Intent(this, ActivityOwnerViewListing::class.java))

                R.id.nav_reviews ->
                    startActivity(Intent(this, OwnerReviewsActivity::class.java))

                R.id.nav_profile ->
                    startActivity(Intent(this, ActivityOwnerProfile::class.java))

                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        setupBottomNav()
    }

    // PROFILE IMAGE LOAD
    private fun loadToolbarProfile() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val base64 = doc.getString("profileImageBase64")

                if (!base64.isNullOrEmpty()) {

                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    headerProfileToolbar.setImageBitmap(bitmap)
                }
            }
    }

    // BOTTOM NAV
    private fun setupBottomNav() {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_performance

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard -> {
                    startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                    true
                }

                R.id.nav_services -> {
                    startActivity(Intent(this, ActivityOwnerViewListing::class.java))
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

                R.id.nav_performance -> true

                else -> false
            }
        }
    }
    private fun loadDrawerHeader() {

        val headerView = navigationView.getHeaderView(0)

        val headerName = headerView.findViewById<TextView>(R.id.headerName)
        val headerRole = headerView.findViewById<TextView>(R.id.headerRole)
        val headerProfileDrawer =
            headerView.findViewById<ImageView>(R.id.headerProfile)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
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


    // LISTING RESUME LOGIC
    private fun openOrResumeListing() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {

            Toast.makeText(
                this,
                "User not logged in",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        db.collection("owner_verifications")
            .document(uid)
            .get()
            .addOnSuccessListener { ownerDoc ->

                val ownerType =
                    ownerDoc.getString("ownerType")
                        ?.lowercase()

                if (ownerType == null) {

                    Toast.makeText(
                        this,
                        "Owner type missing",
                        Toast.LENGTH_SHORT
                    ).show()

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
                                        Intent(
                                            this,
                                            ActivityOwnerAddNewList1::class.java
                                        )
                                            .putExtra(
                                                "OWNER_TYPE",
                                                ownerType
                                            )
                                    )
                                }

                        } else {

                            startActivity(
                                Intent(
                                    this,
                                    ActivityOwnerSubmissionList1::class.java
                                )
                            )
                        }
                    }
            }
    }
}