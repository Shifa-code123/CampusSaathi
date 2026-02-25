package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ActivityOwnerAddNewList3 : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var ownerType: String? = null

    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    // Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list3)

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

// HEADER CONNECT
        val headerView =
            navigationView.inflateHeaderView(R.layout.owner_drawer_header)

        val headerName =
            headerView.findViewById<TextView>(R.id.headerName)

        val headerRole =
            headerView.findViewById<TextView>(R.id.headerRole)

        val headerProfile =
            headerView.findViewById<ImageView>(R.id.headerProfile)

        val uidDrawer =
            FirebaseAuth.getInstance().currentUser?.uid

        if (uidDrawer != null) {

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uidDrawer)
                .get()
                .addOnSuccessListener {

                    headerName.text =
                        it.getString("fullName") ?: "Owner"

                    headerRole.text =
                        it.getString("role") ?: "Owner"

                    val base64 =
                        it.getString("profileImageBase64")

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
                    }
                }
        }


// MENU CLICK EVENTS
        navigationView.setNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_dashboard ->
                    startActivity(
                        Intent(this, ActivityOwnerDashboard::class.java)
                    )

                R.id.nav_add_listing -> {
                    // Already here
                }

                R.id.nav_submission ->
                    startActivity(
                        Intent(this, ActivityOwnerSubmissionList1::class.java)
                    )

                R.id.nav_my_listing ->
                    startActivity(
                        Intent(this, ActivityOwnerViewListing::class.java)
                    )

                R.id.nav_profile ->
                    startActivity(
                        Intent(this, ActivityOwnerProfile::class.java)
                    )

                R.id.nav_logout -> {

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

            drawerLayout.closeDrawer(GravityCompat.START)

            true
        }


        db = FirebaseFirestore.getInstance()

        layoutRoom = findViewById(R.id.layoutRoomStep3)
        layoutMess = findViewById(R.id.layoutMessStep3)
        layoutTuition = findViewById(R.id.layoutTuitionStep3)

        fetchOwnerType()

        findViewById<Button>(R.id.btnNextStep3).setOnClickListener {
            saveStep3()
        }
    }

    private fun fetchOwnerType() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("listings")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                ownerType = doc.getString("ownerType")?.lowercase()

                if (ownerType == null) {
                    toast("Owner type missing")
                    return@addOnSuccessListener
                }

                showCorrectLayout()

                // 🔥 Pre-fill amenities if exists
                val amenities =
                    (doc.get("amenities") as? List<*>)?.map { it.toString() } ?: emptyList()

                restoreCheckboxes(amenities)
            }
    }

    private fun showCorrectLayout() {

        layoutRoom.visibility = View.GONE
        layoutMess.visibility = View.GONE
        layoutTuition.visibility = View.GONE

        when (ownerType) {
            "room", "room_pg" -> layoutRoom.visibility = View.VISIBLE
            "mess" -> layoutMess.visibility = View.VISIBLE
            "tuition" -> layoutTuition.visibility = View.VISIBLE
        }
    }

    private fun saveStep3() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val amenities = mutableListOf<String>()

        when (ownerType) {

            "room", "room_pg" -> {

                addCB(R.id.cbWifi, "Wi-Fi", amenities)
                addCB(R.id.cbWater, "Water Supply", amenities)
                addCB(R.id.cbElectricity, "Electricity Included", amenities)
                addCB(R.id.cbFanLight, "Fan and Light", amenities)
                addCB(R.id.cbBed, "Bed and Mattress", amenities)
                addCB(R.id.cbStudyTable, "Study Table", amenities)
                addCB(R.id.cbCupboard, "Cupboard / Wardrobe", amenities)
                addCB(R.id.cbHotWater, "Hot Water", amenities)
                addCB(R.id.cbCleaning, "Daily Cleaning", amenities)
                addCB(R.id.cbToiletType, "Western / Indian Toilet", amenities)
                addCB(R.id.cbVisitors, "Visitors Allowed", amenities)
                addCB(R.id.cbOutsideFood, "Outside Food Allowed", amenities)

                val bathGroup = findViewById<RadioGroup>(R.id.rgBathroomType)
                if (bathGroup.checkedRadioButtonId != -1) {
                    amenities.add(
                        findViewById<RadioButton>(bathGroup.checkedRadioButtonId).text.toString()
                    )
                }
            }

            "mess" -> {

                addCB(R.id.cbBreakfast, "Breakfast", amenities)
                addCB(R.id.cbLunch, "Lunch", amenities)
                addCB(R.id.cbDinner, "Dinner", amenities)
                addCB(R.id.cbFestivalMeal, "Festival Special Meals", amenities)
                addCB(R.id.cbTiffin, "Tiffin Service", amenities)
                addCB(R.id.cbDelivery, "Home Delivery", amenities)
                addCB(R.id.cbTrial, "Trial Available", amenities)

                val messType = findViewById<RadioGroup>(R.id.rgMessType)
                if (messType.checkedRadioButtonId != -1) {
                    amenities.add(
                        findViewById<RadioButton>(messType.checkedRadioButtonId).text.toString()
                    )
                }
            }

            "tuition" -> {

                addCB(R.id.cbBoard, "Whiteboard / Digital Board", amenities)
                addCB(R.id.cbNotes, "Printed Notes", amenities)
                addCB(R.id.cbRecorded, "Recorded Lectures", amenities)
                addCB(R.id.cbDoubt, "Doubt Solving Sessions", amenities)
                addCB(R.id.cbTestSeries, "Test Series", amenities)

                val tuitionMode = findViewById<RadioGroup>(R.id.rgTuitionMode)
                if (tuitionMode.checkedRadioButtonId != -1) {
                    amenities.add(
                        findViewById<RadioButton>(tuitionMode.checkedRadioButtonId).text.toString()
                    )
                }
            }
        }

        val data = hashMapOf(
            "amenities" to amenities,
            "currentStep" to 4
        )

        db.collection("listings")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {

                toast("Step 3 saved successfully")

                startActivity(
                    Intent(this, ActivityOwnerAddNewList4::class.java)
                )
            }
            .addOnFailureListener {
                toast(it.message ?: "Firebase error")
            }
    }

    private fun restoreCheckboxes(saved: List<String>) {

        val allIds = listOf(
            R.id.cbWifi, R.id.cbWater, R.id.cbElectricity,
            R.id.cbFanLight, R.id.cbBed, R.id.cbStudyTable,
            R.id.cbCupboard, R.id.cbHotWater, R.id.cbCleaning,
            R.id.cbToiletType, R.id.cbVisitors, R.id.cbOutsideFood,
            R.id.cbBreakfast, R.id.cbLunch, R.id.cbDinner,
            R.id.cbFestivalMeal, R.id.cbTiffin, R.id.cbDelivery,
            R.id.cbTrial, R.id.cbBoard, R.id.cbNotes,
            R.id.cbRecorded, R.id.cbDoubt, R.id.cbTestSeries
        )

        for (id in allIds) {
            val cb = findViewById<CheckBox?>(id)
            cb?.let {
                if (saved.contains(it.text.toString())) {
                    it.isChecked = true
                }
            }
        }
    }

    private fun addCB(id: Int, value: String, list: MutableList<String>) {
        val cb = findViewById<CheckBox>(id)
        if (cb.isChecked) list.add(value)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
