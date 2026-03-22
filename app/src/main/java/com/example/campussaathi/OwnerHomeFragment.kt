package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerHomeFragment : Fragment() {

    private lateinit var profileImage: ImageView

    // 🔥 NEW
    private lateinit var tvTotalServices: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvTotalReviews: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_owner_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        profileImage = view.findViewById(R.id.profileImage)

        val txtOwnerName = view.findViewById<TextView>(R.id.txtOwnerName)
        val txtOwnerType = view.findViewById<TextView>(R.id.txtOwnerType)

        // 🔥 NEW bindings
        tvTotalServices = view.findViewById(R.id.tvTotalServices)
        tvRating = view.findViewById(R.id.tvRating)
        tvTotalReviews = view.findViewById(R.id.tvTotalReviews)

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        uid?.let {
            db.collection("owner_verifications").document(it).get()
                .addOnSuccessListener { doc ->
                    txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                    txtOwnerType.text = doc.getString("ownerType") ?: "Owner"
                }
        }

        loadProfileImage()
        setupButtons(view)

        // 🔥 NEW
        loadDashboardData()
        setupCardClicks(view)
    }

    // =========================
    // 🔥 DASHBOARD LOGIC
    // =========================

    private fun loadDashboardData() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("services")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { serviceDocs ->

                val serviceCount = serviceDocs.size()
                tvTotalServices.text = serviceCount.toString()

                val serviceIds = serviceDocs.map { it.id }

                if (serviceIds.isNotEmpty()) {
                    loadRatings(serviceIds)
                } else {
                    tvRating.text = "0.0"
                    tvTotalReviews.text = "(0 Reviews)"
                }
            }
    }

    private fun loadRatings(serviceIds: List<String>) {

        db.collection("ratings")
            .get()
            .addOnSuccessListener { ratingDocs ->

                var total = 0
                var sum = 0f

                for (doc in ratingDocs) {

                    val serviceId = doc.getString("serviceId") ?: continue

                    if (serviceIds.contains(serviceId)) {

                        val rating = doc.getLong("rating")?.toInt() ?: 0

                        if (rating in 1..5) {
                            sum += rating
                            total++
                        }
                    }
                }

                if (total == 0) {
                    tvRating.text = "0.0"
                    tvTotalReviews.text = "(0 Reviews)"
                } else {
                    val avg = sum / total
                    tvRating.text = String.format("%.1f", avg)
                    tvTotalReviews.text = "($total Reviews)"
                }
            }
    }

    // =========================
    // 🔥 CARD CLICKS
    // =========================

    private fun setupCardClicks(view: View) {

        val nav = activity as? NavigationHandler

        val cardServices = view.findViewById<View>(R.id.cardTotalServices)
        val cardRating = view.findViewById<View>(R.id.cardRating)

        cardServices.setOnClickListener {
            nav?.navigateTo(R.id.nav_services) // Tab 2
        }

        cardRating.setOnClickListener {
            nav?.navigateTo(R.id.nav_review) // Tab 4
        }
    }

    // =========================
    // EXISTING CODE (UNCHANGED)
    // =========================

    private fun setupButtons(view: View) {
        val nav = activity as? NavigationHandler

        // Changed from TextView to View because R.id.txtBusinessProfile is a RelativeLayout in XML
        view.findViewById<View>(R.id.txtBusinessProfile).setOnClickListener {
            nav?.navigateTo(R.id.nav_profile)
        }

        view.findViewById<Button>(R.id.btnViewListing).setOnClickListener {
            nav?.navigateTo(R.id.nav_services)
        }

        view.findViewById<Button>(R.id.btnEditListing).setOnClickListener {
            startActivity(Intent(requireContext(), EditServicesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnAddListing).setOnClickListener {
            nav?.navigateTo(R.id.nav_add)
        }
    }

    private fun loadProfileImage() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val base64 = doc.getString("profileImageBase64")
                try {
                    if (!base64.isNullOrEmpty()) {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        profileImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {}
            }
    }
}