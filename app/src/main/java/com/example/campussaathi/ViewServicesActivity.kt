package com.example.campussaathi

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewServicesActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var serviceDocId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_services)

        loadService()

        val btnDelete = findViewById<TextView>(R.id.btnDelete)

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun loadService() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val txtServiceInfo = findViewById<TextView>(R.id.txtServiceInfo)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)
        val layoutImages = findViewById<LinearLayout>(R.id.layoutImages)

        // IMPORTANT FIX
        layoutImages.removeAllViews()

        db.collection("services")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { query ->

                if (query.isEmpty) return@addOnSuccessListener

                val doc = query.documents[0]

                serviceDocId = doc.id

                val name = doc.getString("serviceName") ?: "-"
                val description = doc.getString("description") ?: "-"
                val contact = doc.getString("contact") ?: "-"

                txtServiceInfo.text =
                    "Name: $name\n\nDescription: $description\n\nContact: $contact"

                // STATUS UI
                val status = doc.getString("status") ?: "pending"

                txtStatus.text = status.replaceFirstChar { it.uppercase() }

                when (status) {
                    "pending" -> txtStatus.setBackgroundResource(R.drawable.status_pending)
                    "approved" -> txtStatus.setBackgroundResource(R.drawable.status_accepted)
                    "rejected" -> txtStatus.setBackgroundResource(R.drawable.status_rejected)
                }

                // LOAD ALL PHOTOS
                val photos = doc.get("photos") as? List<*>

                photos?.forEach { photo ->

                    val url = photo.toString()

                    val imageView = ImageView(this)

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500
                    )

                    params.setMargins(0, 12, 0, 20)

                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    Glide.with(this)
                        .load(url)
                        .into(imageView)

                    layoutImages.addView(imageView)
                }
            }
    }

    private fun showDeleteConfirmation() {

        AlertDialog.Builder(this)
            .setTitle("Delete Service")
            .setMessage("Are you sure you want to delete this service?")
            .setPositiveButton("Delete") { _, _ ->
                deleteService()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteService() {

        val id = serviceDocId ?: return

        db.collection("services")
            .document(id)
            .delete()
            .addOnSuccessListener {

                Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, ActivityOwnerDashboard::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show()
            }
    }
}