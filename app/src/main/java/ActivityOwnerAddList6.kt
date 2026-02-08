package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ActivityOwnerAddNewList6 : AppCompatActivity() {

    private lateinit var txtPropertyInfo: TextView
    private lateinit var txtRentInfo: TextView
    private lateinit var txtAmenities: TextView
    private lateinit var txtLocation: TextView
    private lateinit var cbConfirm: CheckBox

    // Edit buttons
    private lateinit var btnEditProperty: TextView
    private lateinit var btnEditRent: TextView
    private lateinit var btnEditAmenities: TextView
    private lateinit var btnEditLocation: TextView

    private lateinit var btnSubmit: Button
    private lateinit var btnDraft: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var listingId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_add_list6)

        listingId = intent.getStringExtra("listingId") ?: ""

        // Bind views
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

        loadPreviewData()

        // ✅ Submit
        btnSubmit.setOnClickListener {
            if (!cbConfirm.isChecked) {
                Toast.makeText(this, "Please confirm details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            submitListing()
        }

        // ✅ Save as Draft
        btnDraft.setOnClickListener {
            saveAsDraft()
        }

        // ✅ Edit navigation
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

    // 🔵 Load preview data from Firestore
    private fun loadPreviewData() {
        if (listingId.isEmpty()) return

        db.collection("listings")
            .document(listingId)
            .get()
            .addOnSuccessListener { doc ->

                txtPropertyInfo.text =
                    "${doc.getString("title")}\n" +
                            "Type: ${doc.getString("type")}\n" +
                            "Preferred Gender: ${doc.getString("gender")}"

                txtRentInfo.text =
                    "Monthly Rent: ₹${doc.getString("rent")}\n" +
                            "Security Deposit: ₹${doc.getString("deposit")}\n" +
                            "Availability: ${doc.getString("availability")}"

                txtAmenities.text =
                    (doc.get("amenities") as? List<*>)?.joinToString(", ") ?: "-"

                txtLocation.text =
                    "${doc.getString("area")}\n" +
                            "Landmark: ${doc.getString("landmark")}\n" +
                            "City: ${doc.getString("city")}"
            }
    }

    // 🟢 Submit for approval
    private fun submitListing() {
        if (listingId.isEmpty()) return

        db.collection("listings")
            .document(listingId)
            .update("status", "pending")
            .addOnSuccessListener {
                Toast.makeText(this, "Submitted for approval", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    // 🟡 Save draft
    private fun saveAsDraft() {
        if (listingId.isEmpty()) return

        db.collection("listings")
            .document(listingId)
            .update("status", "draft")
            .addOnSuccessListener {
                Toast.makeText(this, "Saved as draft", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    // 🔁 Open step with listingId
    private fun openStep(clazz: Class<*>) {
        val intent = Intent(this, clazz)
        intent.putExtra("listingId", listingId)
        startActivity(intent)
    }
}
