package com.example.campussaathi

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.campussaathi.databinding.AdminActivityManageDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminManageDetailActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityManageDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private var docId: String? = null
    private var type: String? = null
    private var currentData: Map<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityManageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId = intent.getStringExtra("DOC_ID")
        type = intent.getStringExtra("TYPE")

        setupAdminHeader(
            headerBinding = binding.adminManageDetailHeader,
            title = "Details",
            showBack = true
        )

        if (docId != null && type != null) {
            fetchDetails()
        }

        binding.btnManageDelete.setOnClickListener { confirmDelete() }
        binding.btnManageEdit.setOnClickListener { showEditDialog() }
    }

    private fun fetchDetails() {
        binding.adminManageDetailProgressBar.visibility = View.VISIBLE
        val collection = getCollectionName()
        if (collection == null) {
            binding.adminManageDetailProgressBar.visibility = View.GONE
            return
        }

        db.collection(collection).document(docId!!).get()
            .addOnSuccessListener { document ->
                binding.adminManageDetailProgressBar.visibility = View.GONE
                if (document.exists()) {
                    currentData = document.data
                    
                    // Handle Owner Proof Image
                    if (type == "owner") {
                        val base64 = document.getString("proofImageBase64")
                        loadBase64Image(base64)
                    } else {
                        binding.adminProofImage.visibility = View.GONE
                    }
                    
                    displayData(currentData)
                }
            }
            .addOnFailureListener { e ->
                binding.adminManageDetailProgressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBase64Image(base64String: String?) {
        try {
            if (!base64String.isNullOrEmpty()) {
                // Remove base64 header if present
                val clean = base64String.substringAfter("base64,", base64String)
                val decodedBytes = Base64.decode(clean, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) {
                    binding.adminProofImage.visibility = View.VISIBLE
                    binding.adminProofImage.setImageBitmap(bitmap)
                } else {
                    binding.adminProofImage.visibility = View.GONE
                }
            } else {
                binding.adminProofImage.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.adminProofImage.visibility = View.GONE
        }
    }

    private fun getCollectionName(): String? {
        return when (type) {
            "student" -> "users"
            "owner" -> "owner_verifications"
            "volunteer" -> "volunteer_requests"
            "cityhelp" -> "cityhelp"
            else -> null
        }
    }

    private fun displayData(data: Map<String, Any?>?) {
        binding.adminManageDetailInfoContainer.removeAllViews()
        
        data?.forEach { (key, value) ->
            // Skip image data and internal fields
            if (key == "photos" || key == "password" || key == "proofImageBase64") return@forEach
            
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
            binding.adminManageDetailInfoContainer.addView(itemView)
            binding.adminManageDetailInfoContainer.addView(divider)
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
        val collection = getCollectionName() ?: return
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

        val nameKey = when(type) {
            "student" -> "name"
            "owner" -> "fullName"
            "volunteer" -> "name"
            "cityhelp" -> "serviceName"
            else -> "name"
        }

        val nameField = EditText(this).apply {
            hint = "Name/Title"
            setText(currentData?.get(nameKey)?.toString())
        }
        
        layout.addView(nameField)

        AlertDialog.Builder(this)
            .setTitle("Edit Info")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val updates = mapOf(nameKey to nameField.text.toString())
                saveUpdates(updates)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveUpdates(updates: Map<String, Any>) {
        val collection = getCollectionName() ?: return
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
