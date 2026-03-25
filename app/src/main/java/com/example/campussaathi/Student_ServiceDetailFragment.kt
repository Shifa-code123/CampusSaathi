package com.example.campussaathi

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.campussaathi.databinding.FragmentStudentServiceDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class Student_ServiceDetailFragment : Fragment() {

    private var _binding: FragmentStudentServiceDetailBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentServiceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val serviceId = arguments?.getString("serviceId") ?: return
        (activity as? StudentActivity)?.updateHeaderForFragment("Service Details", isBack = true)
        
        setupActions(serviceId)
        fetchServiceDetails(serviceId)
    }

    private fun setupActions(serviceId: String) {
        val userId = auth.currentUser?.uid ?: return

        // --- LIKE LOGIC ---
        val likeRef = db.collection("likes").document(serviceId)
        val userLikeRef = likeRef.collection("userLikes").document(userId)

        userLikeRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                binding.btnLikeDetail.setImageResource(R.drawable.ic_heart)
                binding.btnLikeDetail.setColorFilter(Color.RED)
            } else {
                binding.btnLikeDetail.setImageResource(R.drawable.ic_heart)
                binding.btnLikeDetail.setColorFilter(Color.DKGRAY)
            }
        }

        likeRef.collection("userLikes").addSnapshotListener { snapshot, _ ->
            val count = snapshot?.size() ?: 0
            binding.txtLikeCountDetail.text = if (count > 0) "$count Likes" else "Like"
        }

        binding.llLike.setOnClickListener {
            userLikeRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) userLikeRef.delete()
                else userLikeRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
            }
        }

        // --- COMMENT LOGIC ---
        binding.llComment.setOnClickListener {
            CommentBottomSheet(serviceId).show(parentFragmentManager, "comments")
        }

        // --- SAVE LOGIC ---
        val saveRef = db.collection("saved").document(userId).collection("services").document(serviceId)
        saveRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                binding.btnSaveDetail.setImageResource(R.drawable.ic_saved)
                binding.btnSaveDetail.setColorFilter(Color.parseColor("#3F6DF6"))
            } else {
                binding.btnSaveDetail.setImageResource(R.drawable.ic_save_outline)
                binding.btnSaveDetail.setColorFilter(Color.DKGRAY)
            }
        }

        binding.llSave.setOnClickListener {
            saveRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    saveRef.delete()
                } else {
                    // Fetch basic info to save (similar to adapter)
                    db.collection("services").document(serviceId).get().addOnSuccessListener { sDoc ->
                        val data = hashMapOf(
                            "serviceId" to serviceId,
                            "serviceName" to sDoc.getString("serviceName"),
                            "ownerId" to sDoc.getString("ownerId"),
                            "photos" to sDoc.get("photos"),
                            "savedAt" to System.currentTimeMillis()
                        )
                        saveRef.set(data)
                    }
                }
            }
        }

        // --- RATE LOGIC ---
        db.collection("ratings").whereEqualTo("serviceId", serviceId).addSnapshotListener { docs, _ ->
            if (docs != null) {
                var total = 0f
                var count = 0
                for (doc in docs) {
                    doc.getDouble("rating")?.let { total += it.toFloat(); count++ }
                }
                val avg = if (count > 0) total / count else 0f
                binding.txtRatingDetail.text = if (count > 0) String.format("%.1f ⭐", avg) else "Rate"
            }
        }

        binding.llRate.setOnClickListener {
            val intent = Intent(requireContext(), RatingActivity::class.java)
            intent.putExtra("SERVICE_ID", serviceId)
            startActivity(intent)
        }
    }

    private fun fetchServiceDetails(serviceId: String) {
        db.collection("services").document(serviceId)
            .get()
            .addOnSuccessListener { document ->
                val service = document.toObject(Student_ServiceModel::class.java)
                service?.let {
                    (activity as? StudentActivity)?.updateHeaderForFragment(it.serviceName, isBack = true)

                    binding.tvDetailName.text = it.serviceName
                    binding.tvDetailDesc.text = it.description
                    binding.tvDetailContact.text = it.contact
                    
                    if (it.latitude != null && it.longitude != null) {
                        val results = FloatArray(1)
                        Location.distanceBetween(20.7270, 76.5667, it.latitude, it.longitude, results)
                        val distanceInKm = results[0] / 1000
                        binding.tvDetailDistance.text = String.format(Locale.US, "%.1f km from campus (GPK)", distanceInKm)
                    }

                    if (it.photos.isNotEmpty()) {
                        binding.vpImages.adapter = Student_ImageAdapter(it.photos)
                    }

                    binding.btnCall.setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${service.contact}"))
                        startActivity(intent)
                    }

                    binding.btnDirections.setOnClickListener {
                        if (service.latitude != null && service.longitude != null) {
                            val uri = Uri.parse("google.navigation:q=${service.latitude},${service.longitude}")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
