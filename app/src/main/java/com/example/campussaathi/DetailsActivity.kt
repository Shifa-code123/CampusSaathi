package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ActivityDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import com.example.campussaathi.utils.DrawerManager
import com.google.firebase.auth.FirebaseAuth

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var photos: ArrayList<String>

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var phone: String = ""

    private lateinit var ownerId: String
    private lateinit var serviceName: String
    private lateinit var serviceId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Back button instead of menu
        binding.header.ivMenu.setImageResource(R.drawable.ic_back)
        binding.header.ivMenu.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //drawer navigation logic
        DrawerManager.setupDrawer(
            this,
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        binding.header.tvHeaderTitle.text = "Details"

        // menu button
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        photos = intent.getStringArrayListExtra("PHOTOS") ?: ArrayList()
        serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""
        ownerId = intent.getStringExtra("OWNER_ID") ?: ""
        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        latitude = intent.getDoubleExtra("LAT",0.0)
        longitude = intent.getDoubleExtra("LNG",0.0)
        phone = intent.getStringExtra("PHONE") ?: ""
        val description = intent.getStringExtra("DESCRIPTION") ?: ""

        binding.txtServiceName.text = serviceName
        binding.txtDescription.text = description

        val db = FirebaseFirestore.getInstance()

        // Owner name
        db.collection("owner_verifications")
            .document(ownerId)
            .get()
            .addOnSuccessListener { doc ->
                binding.txtOwnerName.text = doc.getString("fullName") ?: "Owner"
            }

        // Owner profile image
        db.collection("posts")
            .whereEqualTo("ownerId", ownerId)
            .limit(1)
            .get()
            .addOnSuccessListener { docs ->

                if (!docs.isEmpty && !isDestroyed) {

                    var url = docs.documents[0].getString("business_pic")

                    if (url.isNullOrEmpty()) {
                        url = docs.documents[0].getString("img")
                    }

                    if (!url.isNullOrEmpty()) {

                        Glide.with(binding.imgOwnerProfile)
                            .load(url)
                            .circleCrop()
                            .into(binding.imgOwnerProfile)
                    }
                }
            }

        // Image slider
        binding.imageSlider.adapter = PhotosAdapter(photos)
        setupDots(photos.size)

        binding.imageSlider.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setActiveDot(position)
                }
            }
        )

        // Direction
        binding.btnDirection.setOnClickListener {

            val uri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")

            startActivity(intent)
        }

        // Call
        binding.btnCall.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")

            startActivity(intent)
        }

        // Rating click
        var lastRating = 0f

        binding.ratingBar.setOnTouchListener { v, event ->

            val ratingBar = v as android.widget.RatingBar

            val newRating = ((event.x / ratingBar.width) * ratingBar.numStars).toInt() + 1

            if (newRating.toFloat() == lastRating) {

                ratingBar.rating = 0f
                lastRating = 0f
                saveRating(0f)

            } else {

                ratingBar.rating = newRating.toFloat()
                lastRating = newRating.toFloat()
                saveRating(lastRating)
            }

            true
        }

        loadAverageRating()

    }


    private fun setupDots(total: Int) {

        binding.dotsLayout.removeAllViews()

        for (i in 0 until total) {

            val dot = ImageView(this)

            dot.setImageResource(R.drawable.inactive_dot)

            val params = LinearLayout.LayoutParams(24, 24)
            params.setMargins(8, 0, 8, 0)

            dot.layoutParams = params

            binding.dotsLayout.addView(dot)
        }

        setActiveDot(0)
    }

    private fun setActiveDot(position: Int) {

        for (i in 0 until binding.dotsLayout.childCount) {

            val dot = binding.dotsLayout.getChildAt(i) as ImageView

            if (i == position) {
                dot.setImageResource(R.drawable.active_dot)
            } else {
                dot.setImageResource(R.drawable.inactive_dot)
            }
        }
    }

    // Save rating
    private fun saveRating(rating: Float) {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val data = hashMapOf(
            "serviceId" to serviceId,
            "studentId" to userId,
            "rating" to rating
        )

        FirebaseFirestore.getInstance()
            .collection("ratings")
            .document(serviceId + "_" + userId)
            .set(data)
            .addOnSuccessListener {

                loadAverageRating()
            }
    }

    // Load average rating
    private fun loadAverageRating() {

        FirebaseFirestore.getInstance()
            .collection("ratings")
            .whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { docs ->

                var total = 0f
                var count = 0

                for (doc in docs) {
                    total += doc.getDouble("rating")?.toFloat() ?: 0f
                    count++
                }

                if (count > 0) {

                    val avg = total / count

                    binding.ratingBar.rating = avg
                    binding.txtRatingValue.text = String.format("%.1f", avg)

                } else {

                    binding.ratingBar.rating = 0f
                    binding.txtRatingValue.text = "0.0"
                }
            }
    }
}