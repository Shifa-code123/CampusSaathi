package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

class ActivityOwnerDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    // ✅ IMPORTANT: class level declare kiya
    private lateinit var headerProfile: ImageView
    private lateinit var profileImage: ImageView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        // ✅ initialize kiya
        headerProfile = findViewById(R.id.headerProfile)
        profileImage = findViewById(R.id.profileImage)

        // ✅ profile image load
        loadProfileImage()

        headerProfile.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerProfile::class.java))
        }

        val txtOwnerName = findViewById<TextView>(R.id.txtOwnerName)
        val txtOwnerType = findViewById<TextView>(R.id.txtOwnerType)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_dashboard

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {

            db.collection("owner_verifications")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        val name = document.getString("fullName") ?: "Owner"
                        val ownerType = document.getString("ownerType") ?: "Owner"

                        txtOwnerName.text = name
                        txtOwnerType.text = ownerType

                    } else {

                        txtOwnerName.text = "Owner"
                        txtOwnerType.text = "null"

                    }
                }
                .addOnFailureListener {

                    txtOwnerName.text = "Owner"
                    txtOwnerType.text = "null"

                }
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

        val headerView =
            navigationView.inflateHeaderView(R.layout.owner_drawer_header)

        val headerName =
            headerView.findViewById<TextView>(R.id.headerName)

        val headerRole =
            headerView.findViewById<TextView>(R.id.headerRole)

        val headerProfileDrawer =
            headerView.findViewById<ImageView>(R.id.headerProfile)

        val uidDrawer =
            FirebaseAuth.getInstance().currentUser?.uid

        val txtBusinessProfile = findViewById<TextView>(R.id.txtBusinessProfile)

        txtBusinessProfile.setOnClickListener {
            val intent = Intent(this, ActivityOwnerPublicProfile::class.java)
            startActivity(intent)
        }

        if (uidDrawer != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uidDrawer)
                .get()
                .addOnSuccessListener { doc ->

                    headerName.text =
                        doc.getString("fullName") ?: "Owner"

                    headerRole.text =
                        doc.getString("role") ?: "Owner"

                    val base64 =
                        doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes =
                            Base64.decode(base64, Base64.DEFAULT)

                        val bitmap =
                            BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size
                            )

                        headerProfileDrawer.setImageBitmap(bitmap)

                    }
                }
        }

        val btnViewListing =
            findViewById<Button>(R.id.btnViewListing)

        btnViewListing.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ActivityOwnerViewListing::class.java
                )
            )

        }

        val btnEditListing =
            findViewById<Button>(R.id.btnEditListing)

        btnEditListing.setOnClickListener {

            val uid =
                FirebaseAuth.getInstance().currentUser?.uid
                    ?: return@setOnClickListener

            db.collection("listings")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    if (!doc.exists()) {

                        Toast.makeText(
                            this,
                            "No listing found",
                            Toast.LENGTH_SHORT
                        ).show()

                        return@addOnSuccessListener

                    }

                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerEditListing::class.java
                        )
                    )

                }
        }

        val btnAddListing =
            findViewById<Button>(R.id.btnAddListing)

        btnAddListing.setOnClickListener {

            openOrResumeListing()

        }

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

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
                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerViewListing::class.java
                        )
                    )

                R.id.nav_profile ->
                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerProfile::class.java
                        )
                    )

                R.id.nav_notifications ->
                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerNotification::class.java
                        )
                    )

                R.id.nav_logout ->
                    logoutUser()

            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard -> true

                R.id.nav_services -> {

                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerViewListing::class.java
                        )
                    )
                    true

                }

                R.id.nav_add -> {

                    openOrResumeListing()
                    true

                }

                R.id.nav_reviews -> {

                    startActivity(
                        Intent(
                            this,
                            OwnerReviewsActivity::class.java
                        )
                    )
                    true

                }

                R.id.nav_performance -> {

                    startActivity(
                        Intent(
                            this,
                            ActivityOwnerPerformance::class.java
                        )
                    )
                    true

                }

                else -> false
            }
        }
    }

    // ✅ IMPORTANT FIX
    override fun onResume() {
        super.onResume()
        loadProfileImage()
    }

    private fun loadProfileImage() {

        val uid =
            FirebaseAuth.getInstance().currentUser?.uid
                ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                try {

                    val base64 =
                        doc.getString("profileImageBase64")

                    if (!base64.isNullOrEmpty()) {

                        val bytes =
                            Base64.decode(base64, Base64.DEFAULT)

                        val bitmap =
                            BitmapFactory.decodeByteArray(
                                bytes,
                                0,
                                bytes.size
                            )

                        headerProfile.setImageBitmap(bitmap)
                        profileImage.setImageBitmap(bitmap)

                    } else {

                        headerProfile.setImageResource(
                            R.drawable.default_avatar
                        )

                        profileImage.setImageResource(
                            R.drawable.default_avatar
                        )

                    }

                } catch (e: Exception) {

                    headerProfile.setImageResource(
                        R.drawable.default_avatar
                    )

                    profileImage.setImageResource(
                        R.drawable.default_avatar
                    )

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

    private fun logoutUser() {

        FirebaseAuth.getInstance().signOut()

        val intent =
            Intent(this, LoginActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}