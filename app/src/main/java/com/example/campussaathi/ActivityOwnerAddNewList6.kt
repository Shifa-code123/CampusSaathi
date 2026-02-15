package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
    private var listingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_new_list6)

        listingId = intent.getStringExtra("LISTING_ID")

        if (listingId == null) {
            Toast.makeText(this, "Listing ID missing", Toast.LENGTH_SHORT).show()
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
            openStep(ActivityOwnerAddNewList1::class.java)
        }

        btnEditRent.setOnClickListener {
            openStep(ActivityOwnerAddNewList2::class.java)
        }

        btnEditAmenities.setOnClickListener {
            openStep(ActivityOwnerAddNewList3::class.java)
        }

        btnEditLocation.setOnClickListener {
            openStep(ActivityOwnerAddNewList4::class.java)
        }
    }

    private fun loadPreviewData() {

        db.collection("listings")
            .document(listingId!!)
            .get()
            .addOnSuccessListener { doc ->

                val title = doc.getString("title") ?: "-"
                val type = doc.getString("type") ?: "-"
                val gender = doc.getString("gender") ?: "-"

                val rent = doc.getString("rent") ?: "-"
                val deposit = doc.getString("deposit") ?: "-"
                val availability = doc.getString("availability") ?: "-"

                val area = doc.getString("area") ?: "-"
                val landmark = doc.getString("landmark") ?: "-"
                val city = doc.getString("city") ?: "-"

                val amenitiesList =
                    (doc.get("amenities") as? List<*>)?.joinToString(", ") ?: "-"

                txtPropertyInfo.text =
                    "$title\nType: $type\nPreferred Gender: $gender"

                txtRentInfo.text =
                    "Monthly Rent: ₹$rent\n" +
                            "Security Deposit: ₹$deposit\n" +
                            "Availability: $availability"

                txtAmenities.text = amenitiesList

                txtLocation.text =
                    "$area\nLandmark: $landmark\nCity: $city"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load preview", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitListing() {

        db.collection("listings")
            .document(listingId!!)
            .set(mapOf("status" to "pending"), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Submitted for approval", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Submission failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveAsDraft() {

        db.collection("listings")
            .document(listingId!!)
            .set(mapOf("status" to "draft"), SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save draft", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openStep(clazz: Class<*>) {
        val intent = Intent(this, clazz)
        intent.putExtra("LISTING_ID", listingId)
        startActivity(intent)
    }
}
