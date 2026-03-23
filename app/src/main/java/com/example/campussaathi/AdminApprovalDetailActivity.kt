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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.AdminActivityApprovalDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminApprovalDetailActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityApprovalDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private var docId: String? = null
    private var type: String? = null
    private var currentData: Map<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityApprovalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        docId = intent.getStringExtra("DOC_ID")
        type = intent.getStringExtra("TYPE")

        setupAdminHeader(
            headerBinding = binding.adminApprovalDetailHeader,
            title = "Approval Details",
            showBack = true
        )

        if (docId != null && type != null) {
            fetchDetails()
        }

        binding.btnAdminApprove.setOnClickListener { updateStatus("approved") }
        binding.btnAdminReject.setOnClickListener { updateStatus("rejected") }
        binding.btnAdminDelete.setOnClickListener { confirmDelete() }
        binding.btnAdminEdit.setOnClickListener { showEditDialog() }
    }

    private fun fetchDetails() {
        binding.adminDetailProgressBar.visibility = View.VISIBLE

        val collection = if (type == "owner") "owner_verifications" else "services"

        db.collection(collection).document(docId!!).get()
            .addOnSuccessListener {
                binding.adminDetailProgressBar.visibility = View.GONE
                if (it.exists()) {
                    currentData = it.data
                    displayData(currentData)
                    handleImages(currentData)
                }
            }
            .addOnFailureListener {
                binding.adminDetailProgressBar.visibility = View.GONE
                Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleImages(data: Map<String, Any?>?) {
        binding.rvAdminDetailImages.visibility = View.GONE
        binding.adminDetailOwnerImageCard.visibility = View.GONE

        if (type == "service") {
            val photos = data?.get("photos") as? List<String>
            if (!photos.isNullOrEmpty()) {
                binding.rvAdminDetailImages.visibility = View.VISIBLE
                binding.rvAdminDetailImages.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                binding.rvAdminDetailImages.adapter = AdminImageAdapter(photos)
            }
        } else {
            val base64 = data?.get("proofImageBase64") as? String
            if (!base64.isNullOrEmpty()) {
                try {
                    val clean = base64.substringAfter("base64,", base64)
                    val bytes = Base64.decode(clean, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        binding.adminDetailOwnerImageCard.visibility = View.VISIBLE
                        binding.ivAdminDetailOwnerImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun displayData(data: Map<String, Any?>?) {
        binding.adminDetailInfoContainer.removeAllViews()

        data?.forEach { (key, value) ->
            if (
                key.equals("photos", true) ||
                key.equals("idProofText", true) ||
                key.equals("serviceProofText", true) ||
                key.equals("proofImageBase64", true)
            ) return@forEach

            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 16)
            }

            val label = TextView(this).apply {
                text = key.replaceFirstChar { it.uppercase() }
                textSize = 12f
                setTextColor(getColor(R.color.admin_text_secondary))
            }

            val valueTv = TextView(this).apply {
                text = value?.toString() ?: "N/A"
                textSize = 16f
                setTextColor(getColor(R.color.admin_text_primary))
            }

            item.addView(label)
            item.addView(valueTv)

            binding.adminDetailInfoContainer.addView(item)
        }
    }

    private fun updateStatus(status: String) {
        val collection = if (type == "owner") "owner_verifications" else "services"

        db.collection(collection).document(docId!!)
            .update("status", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete?")
            .setPositiveButton("Yes") { _, _ -> deleteDocument() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteDocument() {
        val collection = if (type == "owner") "owner_verifications" else "services"

        db.collection(collection).document(docId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun showEditDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val name = EditText(this)
        val phone = EditText(this)

        layout.addView(name)
        layout.addView(phone)

        AlertDialog.Builder(this)
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val updates = if (type == "owner") {
                    mapOf("fullName" to name.text.toString())
                } else {
                    mapOf("serviceName" to name.text.toString())
                }
                saveUpdates(updates)
            }
            .show()
    }

    private fun saveUpdates(map: Map<String, Any>) {
        val collection = if (type == "owner") "owner_verifications" else "services"

        db.collection(collection).document(docId!!)
            .update(map)
            .addOnSuccessListener { fetchDetails() }
    }
}