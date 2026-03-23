package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminActivityVolunteerDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminVolunteerDetailActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityVolunteerDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private var docId: String? = null
    private var type: String? = null
    private var currentData: Map<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityVolunteerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId = intent.getStringExtra("DOC_ID")
        type = intent.getStringExtra("TYPE")

        setupAdminHeader(
            headerBinding = binding.adminVolunteerDetailHeader,
            title = "Volunteer Details",
            showBack = true
        )

        if (docId != null && type != null) {
            fetchDetails()
        }

        binding.btnAdminApproveVol.setOnClickListener { updateStatus("approved") }
        binding.btnAdminRejectVol.setOnClickListener { updateStatus("rejected") }
        binding.btnAdminDeleteVol.setOnClickListener { confirmDelete() }
        binding.btnAdminEditVol.setOnClickListener { showEditDialog() }
    }

    private fun fetchDetails() {
        binding.adminVolunteerDetailProgressBar.visibility = View.VISIBLE
        val collection = if (type == "volunteer") "volunteer_requests" else "cityhelp"
        db.collection(collection).document(docId!!).get()
            .addOnSuccessListener { document ->
                binding.adminVolunteerDetailProgressBar.visibility = View.GONE
                if (document.exists()) {
                    currentData = document.data
                    displayData(currentData)
                    handleImages(currentData)
                }
            }
            .addOnFailureListener { e ->
                binding.adminVolunteerDetailProgressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleImages(data: Map<String, Any?>?) {
        if (type == "cityhelp") {
            @Suppress("UNCHECKED_CAST")
            val photos = data?.get("photos") as? List<String>
            if (!photos.isNullOrEmpty()) {
                binding.rvAdminVolunteerImages.visibility = View.VISIBLE
                binding.rvAdminVolunteerImages.layoutManager = 
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvAdminVolunteerImages.adapter = AdminImageAdapter(photos)
            } else {
                binding.ivAdminVolunteerPlaceholder.visibility = View.VISIBLE
            }
        } else {
            binding.ivAdminVolunteerPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun displayData(data: Map<String, Any?>?) {
        binding.adminVolunteerInfoContainer.removeAllViews()
        
        data?.forEach { (key, value) ->
            if (key == "photos" || key == "idProofText") return@forEach
            
            val itemView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 16)
            }
            
            val labelTv = TextView(this).apply {
                text = key.replaceFirstChar { it.uppercase() }
                textSize = 12f
                setTextColor(resources.getColor(R.color.admin_text_secondary, theme))
            }
            
            val valueTv = TextView(this).apply {
                text = value?.toString() ?: "N/A"
                textSize = 16f
                setTextColor(resources.getColor(R.color.admin_text_primary, theme))
                setPadding(0, 4, 0, 0)
            }
            
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(resources.getColor(R.color.admin_divider, theme))
                alpha = 0.5f
            }
            
            itemView.addView(labelTv)
            itemView.addView(valueTv)
            binding.adminVolunteerInfoContainer.addView(itemView)
            binding.adminVolunteerInfoContainer.addView(divider)
        }
    }

    private fun updateStatus(status: String) {
        val collection = if (type == "volunteer") "volunteer_requests" else "cityhelp"
        db.collection(collection).document(docId!!)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Status updated to $status", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Record")
            .setMessage("Are you sure you want to delete this permanently?")
            .setPositiveButton("Delete") { _, _ -> deleteRecord() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteRecord() {
        val collection = if (type == "volunteer") "volunteer_requests" else "cityhelp"
        db.collection(collection).document(docId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }

        val nameField = EditText(this).apply {
            hint = if (type == "volunteer") "Name" else "Service Name"
            setText(if (type == "volunteer") currentData?.get("name")?.toString() else currentData?.get("serviceName")?.toString())
        }
        
        val phoneField = EditText(this).apply {
            hint = "Contact"
            setText(if (type == "volunteer") currentData?.get("phone")?.toString() else currentData?.get("contact")?.toString())
        }

        layout.addView(nameField)
        layout.addView(phoneField)

        AlertDialog.Builder(this)
            .setTitle("Edit Info")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val updates = if (type == "volunteer") {
                    mapOf("name" to nameField.text.toString(), "phone" to phoneField.text.toString())
                } else {
                    mapOf("serviceName" to nameField.text.toString(), "contact" to phoneField.text.toString())
                }
                saveUpdates(updates)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUpdates(updates: Map<String, Any>) {
        val collection = if (type == "volunteer") "volunteer_requests" else "cityhelp"
        db.collection(collection).document(docId!!)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                fetchDetails()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}