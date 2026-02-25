package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
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
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView

class ActivityOwnerAddNewList2 : AppCompatActivity() {

    private lateinit var layoutRoom: LinearLayout
    private lateinit var layoutMess: LinearLayout
    private lateinit var layoutTuition: LinearLayout

    // Room
    private lateinit var etRoomRent: EditText
    private lateinit var etRoomDeposit: EditText
    private lateinit var rgRentType: RadioGroup
    private lateinit var rgAvailability: RadioGroup
    private lateinit var etTotalUnits: EditText
    private lateinit var etAvailableUnits: EditText
    private lateinit var etAvailableFrom: EditText

    // Mess
    private lateinit var etMessMonthly: EditText
    private lateinit var etMessDaily: EditText

    // Tuition
    private lateinit var etTuitionFees: EditText
    private lateinit var etCourseDuration: EditText

    private lateinit var btnNext: Button
    private lateinit var db: FirebaseFirestore

    private var ownerType: String? = null

    // Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list2)

        // ===== PROFESSIONAL DRAWER SETUP =====

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

        initViews()
        fetchOwnerType()

        btnNext.setOnClickListener {
            saveStep2()
        }
    }

    private fun initViews() {

        layoutRoom = findViewById(R.id.layoutRoomStep2)
        layoutMess = findViewById(R.id.layoutMessStep2)
        layoutTuition = findViewById(R.id.layoutTuitionStep2)

        etRoomRent = findViewById(R.id.etRoomRent)
        etRoomDeposit = findViewById(R.id.etRoomDeposit)
        rgRentType = findViewById(R.id.rgRentType)
        rgAvailability = findViewById(R.id.rgAvailability)
        etTotalUnits = findViewById(R.id.etTotalUnits)
        etAvailableUnits = findViewById(R.id.etAvailableUnits)
        etAvailableFrom = findViewById(R.id.etAvailableFrom)

        etMessMonthly = findViewById(R.id.etMessMonthlyCharge)
        etMessDaily = findViewById(R.id.etMessDailyCharge)

        etTuitionFees = findViewById(R.id.etTuitionFees)
        etCourseDuration = findViewById(R.id.etCourseDuration)

        btnNext = findViewById(R.id.btnNextStep2)

        availabilityLogic()
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

                // 🔥 Pre-fill if exists
                etRoomRent.setText(doc.getString("rent"))
                etRoomDeposit.setText(doc.getString("deposit"))
                etAvailableFrom.setText(doc.getString("availableFrom"))
                etTotalUnits.setText(doc.getString("totalUnits"))
                etAvailableUnits.setText(doc.getString("availableUnits"))

                etMessMonthly.setText(doc.getString("monthlyCharge"))
                etMessDaily.setText(doc.getString("dailyCharge"))

                etTuitionFees.setText(doc.getString("fees"))
                etCourseDuration.setText(doc.getString("duration"))
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

    private fun availabilityLogic() {

        rgAvailability.setOnCheckedChangeListener { _, checkedId ->

            val selected =
                findViewById<RadioButton>(checkedId).text.toString()

            if (selected == "Vacant") {
                etTotalUnits.visibility = View.VISIBLE
                etAvailableUnits.visibility = View.VISIBLE
            } else {
                etAvailableUnits.setText("0")
                etTotalUnits.visibility = View.GONE
                etAvailableUnits.visibility = View.GONE
            }
        }
    }

    private fun saveStep2() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val data = hashMapOf<String, Any>()

        when (ownerType) {

            "room", "room_pg" -> {

                if (etRoomRent.text.isEmpty()
                    || rgRentType.checkedRadioButtonId == -1
                    || rgAvailability.checkedRadioButtonId == -1
                ) {
                    toast("Fill all required room details")
                    return
                }

                data["rent"] = etRoomRent.text.toString()
                data["deposit"] = etRoomDeposit.text.toString()
                data["rentType"] =
                    findViewById<RadioButton>(rgRentType.checkedRadioButtonId).text.toString()
                data["availability"] =
                    findViewById<RadioButton>(rgAvailability.checkedRadioButtonId).text.toString()
                data["totalUnits"] = etTotalUnits.text.toString()
                data["availableUnits"] = etAvailableUnits.text.toString()
                data["availableFrom"] = etAvailableFrom.text.toString()
            }

            "mess" -> {

                if (etMessMonthly.text.isEmpty()) {
                    toast("Enter monthly charges")
                    return
                }

                data["monthlyCharge"] = etMessMonthly.text.toString()
                data["dailyCharge"] = etMessDaily.text.toString()
            }

            "tuition" -> {

                if (etTuitionFees.text.isEmpty()) {
                    toast("Enter tuition fees")
                    return
                }

                data["fees"] = etTuitionFees.text.toString()
                data["duration"] = etCourseDuration.text.toString()
            }
        }

        data["currentStep"] = 3

        db.collection("listings")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {

                toast("Step 2 saved")

                startActivity(
                    Intent(this, ActivityOwnerAddNewList3::class.java)
                )
            }
            .addOnFailureListener {
                toast(it.message ?: "Firebase error")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
