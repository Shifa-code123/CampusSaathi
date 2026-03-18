package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView


class ActivityOwnerEditListing : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private val db = FirebaseFirestore.getInstance()
    private var uid: String? = null
    private var ownerType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_edit_listing)

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
                    true
                }

                R.id.nav_add -> {
                    openOrResumeListing()
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

        loadData()
    }

    private fun loadData() {

        db.collection("listings")
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                ownerType = doc.getString("ownerType")

                val layoutRoom = findViewById<LinearLayout>(R.id.layoutRoom)
                val layoutMess = findViewById<LinearLayout>(R.id.layoutMess)
                val layoutTuition = findViewById<LinearLayout>(R.id.layoutTuition)

                layoutRoom.visibility = View.GONE
                layoutMess.visibility = View.GONE
                layoutTuition.visibility = View.GONE

                when (ownerType) {

                    "room_pg" -> {
                        layoutRoom.visibility = View.VISIBLE

                        findViewById<EditText>(R.id.etPropertyName)
                            .setText(doc.getString("propertyName"))

                        findViewById<EditText>(R.id.etRent)
                            .setText(doc.getString("rent"))

                        findViewById<EditText>(R.id.etTotalUnits)
                            .setText(doc.getString("totalUnits"))

                        findViewById<EditText>(R.id.etAvailableUnits)
                            .setText(doc.getString("availableUnits"))
                    }

                    "mess" -> {
                        layoutMess.visibility = View.VISIBLE

                        findViewById<EditText>(R.id.etMessName)
                            .setText(doc.getString("messName"))

                        findViewById<EditText>(R.id.etMonthlyCharge)
                            .setText(doc.getString("monthlyCharge"))

                        findViewById<EditText>(R.id.etDailyCharge)
                            .setText(doc.getString("dailyCharge"))
                    }

                    "tuition" -> {
                        layoutTuition.visibility = View.VISIBLE

                        findViewById<EditText>(R.id.etTuitionName)
                            .setText(doc.getString("tuitionName"))

                        findViewById<EditText>(R.id.etFees)
                            .setText(doc.getString("fees"))

                        findViewById<EditText>(R.id.etDuration)
                            .setText(doc.getString("duration"))
                    }
                }

                findViewById<EditText>(R.id.etArea)
                    .setText(doc.getString("area"))

                findViewById<EditText>(R.id.etCity)
                    .setText(doc.getString("city"))

                findViewById<EditText>(R.id.etLandmark)
                    .setText(doc.getString("landmark"))
            }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {

        val updates = hashMapOf<String, Any>()

        when (ownerType) {

            "room_pg" -> {
                updates["propertyName"] =
                    findViewById<EditText>(R.id.etPropertyName).text.toString()

                updates["rent"] =
                    findViewById<EditText>(R.id.etRent).text.toString()

                updates["totalUnits"] =
                    findViewById<EditText>(R.id.etTotalUnits).text.toString()

                updates["availableUnits"] =
                    findViewById<EditText>(R.id.etAvailableUnits).text.toString()
            }

            "mess" -> {
                updates["messName"] =
                    findViewById<EditText>(R.id.etMessName).text.toString()

                updates["monthlyCharge"] =
                    findViewById<EditText>(R.id.etMonthlyCharge).text.toString()

                updates["dailyCharge"] =
                    findViewById<EditText>(R.id.etDailyCharge).text.toString()
            }

            "tuition" -> {
                updates["tuitionName"] =
                    findViewById<EditText>(R.id.etTuitionName).text.toString()

                updates["fees"] =
                    findViewById<EditText>(R.id.etFees).text.toString()

                updates["duration"] =
                    findViewById<EditText>(R.id.etDuration).text.toString()
            }
        }

        updates["area"] =
            findViewById<EditText>(R.id.etArea).text.toString()

        updates["city"] =
            findViewById<EditText>(R.id.etCity).text.toString()

        updates["landmark"] =
            findViewById<EditText>(R.id.etLandmark).text.toString()

        db.collection("listings")
            .document(uid!!)
            .update(updates as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show()
                finish()
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
