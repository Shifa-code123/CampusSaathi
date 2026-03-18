package com.example.campussaathi
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.campussaathi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyServicesFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var serviceDocId: String? = null

    private lateinit var txtServiceInfo: TextView
    private lateinit var txtStatus: TextView
    private lateinit var layoutImages: LinearLayout
    private lateinit var btnDelete: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_services, container, false)

        // init views
        txtServiceInfo = view.findViewById(R.id.txtServiceInfo)
        txtStatus = view.findViewById(R.id.txtStatus)
        layoutImages = view.findViewById(R.id.layoutImages)
        btnDelete = view.findViewById(R.id.btnDelete)

        loadService()

        btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        return view
    }

    private fun loadService() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

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

                    val imageView = ImageView(requireContext())

                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        500
                    )

                    params.setMargins(0, 12, 0, 20)

                    imageView.layoutParams = params
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP

                    Glide.with(requireContext())
                        .load(url)
                        .into(imageView)

                    layoutImages.addView(imageView)
                }
            }
    }

    private fun showDeleteConfirmation() {

        AlertDialog.Builder(requireContext())
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

                Toast.makeText(requireContext(), "Service deleted", Toast.LENGTH_SHORT).show()

                startActivity(Intent(requireContext(), ActivityOwnerDashboard::class.java))
                requireActivity().finish()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete service", Toast.LENGTH_SHORT).show()
            }
    }
}