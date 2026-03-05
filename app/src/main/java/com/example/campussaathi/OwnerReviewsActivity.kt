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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerReviewsActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var headerProfileToolbar: ImageView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_reviews)

        // -------------------------
        // TOOLBAR SETUP
        // -------------------------
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        headerProfileToolbar = findViewById(R.id.headerProfile)

        headerProfileToolbar.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }

        loadToolbarProfile()

        // -------------------------
        // DRAWER SETUP
        // -------------------------
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigation_view)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open,
            R.string.close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        loadDrawerHeader()

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
                    true

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



        // -------------------------
        // BOTTOM NAV SETUP
        // -------------------------
        setupBottomNav()
    }

    // =============================
    // LOAD TOOLBAR PROFILE IMAGE
    // =============================
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

    // =============================
    // LOAD DRAWER HEADER DATA
    // =============================
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

    // =============================
    // BOTTOM NAVIGATION
    // =============================
    private fun setupBottomNav() {

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.selectedItemId = R.id.nav_reviews

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

                R.id.nav_reviews -> true

                R.id.nav_performance -> {
                    startActivity(Intent(this, ActivityOwnerPerformance::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun openOrResumeListing() {

        val uid =
            FirebaseAuth.getInstance().currentUser?.uid

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

                            val status =
                                listingDoc.getString("status")

                            val currentStep =
                                listingDoc.getLong("currentStep")
                                    ?.toInt() ?: 1

                            if (status == "pending"
                                || currentStep >= 6
                            ) {

                                startActivity(
                                    Intent(
                                        this,
                                        ActivityOwnerSubmissionList1::class.java
                                    )
                                )

                                return@addOnSuccessListener
                            }

                            when (currentStep) {

                                1 ->
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

                                2 ->
                                    startActivity(
                                        Intent(
                                            this,
                                            ActivityOwnerAddNewList2::class.java
                                        )
                                    )

                                3 ->
                                    startActivity(
                                        Intent(
                                            this,
                                            ActivityOwnerAddNewList3::class.java
                                        )
                                    )

                                4 ->
                                    startActivity(
                                        Intent(
                                            this,
                                            ActivityOwnerAddNewList4::class.java
                                        )
                                    )

                                5 ->
                                    startActivity(
                                        Intent(
                                            this,
                                            ActivityOwnerAddNewList5::class.java
                                        )
                                    )

                                6 ->
                                    startActivity(
                                        Intent(
                                            this,
                                            ActivityOwnerAddNewList6::class.java
                                        )
                                    )
                            }
                        }
                    }
            }
    }
}