package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

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

        // 🔥 DASHBOARD ADD LISTING BUTTON LOGIC
        val btnAddListing = findViewById<android.widget.Button>(R.id.btnAddListing)

        btnAddListing?.setOnClickListener {

            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (uid == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance()
                .collection("owner_verifications")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    val ownerType = doc.getString("ownerType")

                    if (ownerType == null) {
                        Toast.makeText(this, "Owner type missing", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val intent = Intent(this, ActivityOwnerAddNewList1::class.java)
                    intent.putExtra("OWNER_TYPE", ownerType.lowercase())
                    startActivity(intent)
                }
        }


        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_add_listing -> {

                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                    if (uid == null) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                        return@setNavigationItemSelectedListener true
                    }

                    FirebaseFirestore.getInstance()
                        .collection("owner_verifications")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc ->

                            val ownerType = doc.getString("ownerType")

                            if (ownerType == null) {
                                Toast.makeText(this, "Owner type missing", Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }

                            val intent = Intent(this, ActivityOwnerAddNewList1::class.java)
                            intent.putExtra("OWNER_TYPE", ownerType.lowercase())
                            startActivity(intent)
                        }
                }

                R.id.nav_submission -> {
                    startActivity(
                        Intent(this, ActivityOwnerSubmissionList1::class.java)
                    )
                }

                R.id.nav_my_listing -> {
                    startActivity(
                        Intent(this, ActivityOwnerMyList::class.java)
                    )
                }

                R.id.nav_profile -> {
                    startActivity(
                        Intent(this, ActivityOwnerProfile::class.java)
                    )
                }

                R.id.nav_logout -> {
                    logoutUser()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun logoutUser() {

        // 🔥 Proper logout
        FirebaseAuth.getInstance().signOut()

        // Clear backstack completely
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
