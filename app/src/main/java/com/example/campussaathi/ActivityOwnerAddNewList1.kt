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

class ActivityOwnerAddNewList1 : AppCompatActivity() {

    private lateinit var roomLayout: LinearLayout
    private lateinit var messLayout: LinearLayout
    private lateinit var tuitionLayout: LinearLayout

    private lateinit var etPropertyName: EditText
    private lateinit var rgPropertyType: RadioGroup
    private lateinit var rgGenderAllowed: RadioGroup

    private lateinit var etMessName: EditText
    private lateinit var rgMessType: RadioGroup

    private lateinit var etTuitionName: EditText
    private lateinit var rgTuitionType: RadioGroup

    private lateinit var btnNext: Button

    private lateinit var db: FirebaseFirestore
    private var ownerType: String? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    private val uid by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list1)

        if (uid == null) {
            toast("User not logged in")
            finish()
            return
        }

        setupDrawer()

        db = FirebaseFirestore.getInstance()

        initViews()
        fetchOwnerType()

        btnNext.setOnClickListener {
            saveStep1()
        }
    }

    private fun setupDrawer() {

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


        // ✅ HEADER CONNECT
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


        // ✅ MENU CLICK NAVIGATION
        navigationView.setNavigationItemSelectedListener {

            when (it.itemId) {

                R.id.nav_dashboard ->
                    startActivity(
                        Intent(this, ActivityOwnerDashboard::class.java)
                    )

                R.id.nav_profile ->
                    startActivity(
                        Intent(this, ActivityOwnerProfile::class.java)
                    )

                R.id.nav_my_listing ->
                    startActivity(
                        Intent(this, ActivityOwnerViewListing::class.java)
                    )

                R.id.nav_add_listing -> {
                    // already here
                }

                R.id.nav_submission ->
                    startActivity(
                        Intent(this, ActivityOwnerSubmissionList1::class.java)
                    )

                R.id.nav_logout -> {

                    FirebaseAuth.getInstance().signOut()

                    startActivity(
                        Intent(this, LoginActivity::class.java)
                    )

                    finish()
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START)

            true
        }
    }
    private fun initViews() {

        roomLayout = findViewById(R.id.layoutRoomStep1)
        messLayout = findViewById(R.id.layoutMessStep1)
        tuitionLayout = findViewById(R.id.layoutTuitionStep1)

        etPropertyName = findViewById(R.id.etPropertyName)
        rgPropertyType = findViewById(R.id.rgPropertyType)
        rgGenderAllowed = findViewById(R.id.rgGenderAllowed)

        etMessName = findViewById(R.id.etMessName)
        rgMessType = findViewById(R.id.rgMessType)

        etTuitionName = findViewById(R.id.etTuitionName)
        rgTuitionType = findViewById(R.id.rgTuitionType)

        btnNext = findViewById(R.id.btnNext)
    }

    private fun fetchOwnerType() {

        db.collection("owner_verifications")   // ✅ FIXED
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                ownerType = doc.getString("ownerType")?.lowercase()

                if (ownerType == null) {
                    toast("Owner type missing")
                    return@addOnSuccessListener
                }

                showCorrectLayout()
                prefillIfExists()
            }
            .addOnFailureListener {
                toast("Failed to load owner type")
            }
    }

    private fun prefillIfExists() {

        db.collection("listings")
            .document(uid!!)
            .get()
            .addOnSuccessListener { listingDoc ->

                if (!listingDoc.exists()) return@addOnSuccessListener

                when (ownerType) {

                    "room", "room_pg" -> {
                        etPropertyName.setText(listingDoc.getString("propertyName"))
                    }

                    "mess" -> {
                        etMessName.setText(listingDoc.getString("messName"))
                    }

                    "tuition" -> {
                        etTuitionName.setText(listingDoc.getString("tuitionName"))
                    }
                }
            }
    }

    private fun showCorrectLayout() {

        roomLayout.visibility = View.GONE
        messLayout.visibility = View.GONE
        tuitionLayout.visibility = View.GONE

        when (ownerType) {

            "room", "room_pg" -> roomLayout.visibility = View.VISIBLE
            "mess" -> messLayout.visibility = View.VISIBLE
            "tuition" -> tuitionLayout.visibility = View.VISIBLE

            else -> toast("Invalid owner type")
        }
    }

    private fun saveStep1() {

        val data = hashMapOf<String, Any>()

        when (ownerType) {

            "room", "room_pg" -> {

                if (etPropertyName.text.isEmpty()
                    || rgPropertyType.checkedRadioButtonId == -1
                    || rgGenderAllowed.checkedRadioButtonId == -1) {
                    toast("Fill all Room details")
                    return
                }

                data["ownerType"] = ownerType!!
                data["propertyName"] = etPropertyName.text.toString()
                data["propertyType"] =
                    findViewById<RadioButton>(rgPropertyType.checkedRadioButtonId).text.toString()
                data["genderAllowed"] =
                    findViewById<RadioButton>(rgGenderAllowed.checkedRadioButtonId).text.toString()
            }

            "mess" -> {

                if (etMessName.text.isEmpty()
                    || rgMessType.checkedRadioButtonId == -1) {
                    toast("Fill all Mess details")
                    return
                }

                data["ownerType"] = "mess"
                data["messName"] = etMessName.text.toString()
                data["messType"] =
                    findViewById<RadioButton>(rgMessType.checkedRadioButtonId).text.toString()
            }

            "tuition" -> {

                if (etTuitionName.text.isEmpty()
                    || rgTuitionType.checkedRadioButtonId == -1) {
                    toast("Fill all Tuition details")
                    return
                }

                data["ownerType"] = "tuition"
                data["tuitionName"] = etTuitionName.text.toString()
                data["tuitionType"] =
                    findViewById<RadioButton>(rgTuitionType.checkedRadioButtonId).text.toString()
            }

            else -> {
                toast("Invalid owner type")
                return
            }
        }

        data["status"] = "draft"
        data["currentStep"] = 2

        db.collection("listings")
            .document(uid!!)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                toast("Step 1 saved")
                startActivity(Intent(this, ActivityOwnerAddNewList2::class.java))
                finish()
            }
            .addOnFailureListener {
                toast("Firebase error")
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
