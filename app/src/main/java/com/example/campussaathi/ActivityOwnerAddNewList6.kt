package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ActivityOwnerAddNewList6 : AppCompatActivity() {

    private lateinit var txtPropertyInfo: TextView
    private lateinit var txtRentInfo: TextView
    private lateinit var txtAmenities: TextView
    private lateinit var txtLocation: TextView
    private lateinit var cbConfirm: CheckBox

    private lateinit var btnEditProperty: TextView
    private lateinit var btnEditRent: TextView
    private lateinit var btnEditAmenities: TextView
    private lateinit var btnEditLocation: TextView

    private lateinit var btnSubmit: Button
    private lateinit var btnDraft: Button

    private val db = FirebaseFirestore.getInstance()
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list6)

        uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        loadPreviewData()
        setupClickListeners()
    }

    private fun bindViews() {
        txtPropertyInfo = findViewById(R.id.txtPropertyInfo)
        txtRentInfo = findViewById(R.id.txtRentInfo)
        txtAmenities = findViewById(R.id.txtAmenities)
        txtLocation = findViewById(R.id.txtLocation)
        cbConfirm = findViewById(R.id.cbConfirm)

        btnEditProperty = findViewById(R.id.btnEditProperty)
        btnEditRent = findViewById(R.id.btnEditRent)
        btnEditAmenities = findViewById(R.id.btnEditAmenities)
        btnEditLocation = findViewById(R.id.btnEditLocation)

        btnSubmit = findViewById(R.id.btnSubmit)
        btnDraft = findViewById(R.id.btnDraft)
    }

    private fun setupClickListeners() {

        btnSubmit.setOnClickListener {
            if (!cbConfirm.isChecked) {
                Toast.makeText(this, "Please confirm details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitListing()
        }

        btnDraft.setOnClickListener {
            saveAsDraft()
        }

        btnEditProperty.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerAddNewList1::class.java))
        }

        btnEditRent.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerAddNewList2::class.java))
        }

        btnEditAmenities.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerAddNewList3::class.java))
        }

        btnEditLocation.setOnClickListener {
            startActivity(Intent(this, ActivityOwnerAddNewList4::class.java))
        }
    }

    private fun loadPreviewData() {

        db.collection("listings")
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                val propertyName = doc.getString("propertyName") ?: "-"
                val propertyType = doc.getString("propertyType") ?: "-"
                val gender = doc.getString("genderAllowed") ?: "-"

                val rent = doc.getString("rent") ?: "-"
                val deposit = doc.getString("deposit") ?: "-"
                val availability = doc.getString("availability") ?: "-"

                val area = doc.getString("area") ?: "-"
                val landmark = doc.getString("landmark") ?: "-"
                val city = doc.getString("city") ?: "-"

                val amenitiesList =
                    (doc.get("amenities") as? List<*>)?.joinToString(", ") ?: "-"

                txtPropertyInfo.text =
                    "$propertyName\nType: $propertyType\nPreferred Gender: $gender"

                txtRentInfo.text =
                    "Monthly Rent: ₹$rent\nSecurity Deposit: ₹$deposit\nAvailability: $availability"

                txtAmenities.text = amenitiesList

                txtLocation.text =
                    "$area\nLandmark: $landmark\nCity: $city"
            }
    }

    private fun submitListing() {

        db.collection("listings")
            .document(uid!!)
            .set(mapOf("status" to "pending"), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Submitted for approval", Toast.LENGTH_SHORT).show()

                startActivity(
                    Intent(this, ActivityOwnerSubmissionList1::class.java)
                )


                finish()
            }
    }

    private fun saveAsDraft() {

        db.collection("listings")
            .document(uid!!)
            .set(mapOf("status" to "draft"), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
