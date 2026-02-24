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
import java.util.Calendar
import android.widget.TextView

class ActivityOwnerDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        val txtGreeting = findViewById<TextView>(R.id.txtGreeting)
        val txtOwnerName = findViewById<TextView>(R.id.txtOwnerName)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }

        txtGreeting.text = greeting

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("owner_verifications")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("fullName") ?: "Owner"
                        txtOwnerName.text = name
                    } else {
                        txtOwnerName.text = "Owner"
                    }
                }
                .addOnFailureListener {
                    txtOwnerName.text = "Owner"
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

        val btnViewListing = findViewById<Button>(R.id.btnViewListing)

        btnViewListing.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerViewListing::class.java))
        }


        val btnEditListing = findViewById<Button>(R.id.btnEditListing)

        btnEditListing.setOnClickListener {

            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            db.collection("listings")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    if (!doc.exists()) {
                        Toast.makeText(this, "No listing found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    startActivity(
                        Intent(this, ActivityOwnerEditListing::class.java)
                    )
                }
        }


        val btnAddListing = findViewById<Button>(R.id.btnAddListing)

        btnAddListing.setOnClickListener {
            openOrResumeListing()
        }

        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_add_listing -> openOrResumeListing()

                R.id.nav_submission -> {
                    startActivity(Intent(this, ActivityOwnerSubmissionList1::class.java))
                }

                R.id.nav_my_listing -> {
                    startActivity(Intent(this, ActivityOwnerMyList::class.java))
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ActivityOwnerProfile::class.java))
                }

                R.id.nav_logout -> logoutUser()
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
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
