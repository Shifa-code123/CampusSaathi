package com.example.campussaathi

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.AlertDialog
import android.content.Intent
import android.widget.Toast

class ActivityOwnerViewListing : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_view_listing)


        uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            finish()
            return
        }

        loadListing()

        val btnDelete = findViewById<TextView>(R.id.btnDelete)

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadListing() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val txtStatus = findViewById<TextView>(R.id.txtStatus)

        FirebaseFirestore.getInstance()
            .collection("listings")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                val status = doc.getString("status") ?: "pending"

                txtStatus.text = status.replaceFirstChar { it.uppercase() }

                when (status) {
                    "pending" -> txtStatus.setBackgroundResource(R.drawable.status_pending)
                    "approved" -> txtStatus.setBackgroundResource(R.drawable.status_accepted)
                    "rejected" -> txtStatus.setBackgroundResource(R.drawable.status_rejected)
                }

            }

        db.collection("listings")
            .document(uid!!)
            .get()
            .addOnSuccessListener { doc ->

                val ownerType = doc.getString("ownerType") ?: ""

                val txtBasicInfo = findViewById<TextView>(R.id.txtBasicInfo)
                val txtPricing = findViewById<TextView>(R.id.txtPricing)
                val txtAmenities = findViewById<TextView>(R.id.txtAmenities)
                val txtLocation = findViewById<TextView>(R.id.txtLocation)
                val txtCoordinates = findViewById<TextView>(R.id.txtCoordinates)
                val layoutImages = findViewById<LinearLayout>(R.id.layoutImages)

                when (ownerType) {

                    "room_pg" -> {
                        txtBasicInfo.text =
                            "Name: ${doc.getString("propertyName")}\n" +
                                    "Type: ${doc.getString("propertyType")}\n" +
                                    "Total Rooms: ${doc.getString("totalUnits")}\n" +
                                    "Vacant: ${doc.getString("availableUnits")}"

                        txtPricing.text =
                            "Rent: ₹${doc.getString("rent")} (${doc.getString("rentType")})"
                    }

                    "mess" -> {
                        txtBasicInfo.text =
                            "Mess: ${doc.getString("messName")}\n" +
                                    "Type: ${doc.getString("messType")}"

                        txtPricing.text =
                            "Monthly: ₹${doc.getString("monthlyCharge")}\n" +
                                    "Daily: ₹${doc.getString("dailyCharge")}"
                    }

                    "tuition" -> {
                        txtBasicInfo.text =
                            "Tuition: ${doc.getString("tuitionName")}\n" +
                                    "Type: ${doc.getString("tuitionType")}"

                        txtPricing.text =
                            "Fees: ₹${doc.getString("fees")}\n" +
                                    "Duration: ${doc.getString("duration")} days"
                    }
                }

                // Amenities
                val amenitiesList =
                    (doc.get("amenities") as? List<*>)?.joinToString(", ") ?: "-"
                txtAmenities.text = amenitiesList

                // Location
                txtLocation.text =
                    "${doc.getString("area")}\n" +
                            "Landmark: ${doc.getString("landmark")}\n" +
                            "City: ${doc.getString("city")}\n" +
                            "Pincode: ${doc.getString("pincode")}"

                txtCoordinates.text =
                    "Latitude: ${doc.getDouble("latitude")}\n" +
                            "Longitude: ${doc.getDouble("longitude")}"

                // Load Images (Base64)
                val images =
                    doc.get("imagesBase64") as? List<*>

                images?.forEach { base64 ->

                    val bytes = Base64.decode(base64 as String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    val imageView = ImageView(this)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500
                    )
                    params.setMargins(0, 12, 0, 20)

                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setImageBitmap(bitmap)

                    layoutImages.addView(imageView)
                }
            }
    }

    // ✅ ADDED: Show confirmation before delete
    private fun showDeleteConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this listing? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteListing()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    // ✅ ADDED: Delete listing from Firestore
    private fun deleteListing() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("listings")
            .document(uid)
            .delete()
            .addOnSuccessListener {

                Toast.makeText(this, "Listing deleted", Toast.LENGTH_SHORT).show()

                // Navigate back to dashboard after delete
                startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete listing", Toast.LENGTH_SHORT).show()
            }
    }
}
