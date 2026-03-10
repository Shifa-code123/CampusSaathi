package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.campussaathi.databinding.ActivityDetailsBinding
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var photos: ArrayList<String>

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var phone: String = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var ownerId: String
    private lateinit var serviceName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // photos
        photos = intent.getStringArrayListExtra("PHOTOS") ?: ArrayList()
        binding.imageSlider.adapter = PhotosAdapter(photos)

        setupDots(photos.size)

        binding.imageSlider.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    setActiveDot(position)
                }
            }
        )

        // receive intent data
        serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""
        ownerId = intent.getStringExtra("OWNER_ID") ?: ""

        Log.d("DETAIL_OWNER_ID", ownerId)

        binding.txtServiceName.text = serviceName

        val db = FirebaseFirestore.getInstance()

        // OWNER NAME
        db.collection("owner_verifications")
            .document(ownerId)
            .get()
            .addOnSuccessListener { doc ->
                binding.txtOwnerName.text = doc.getString("fullName") ?: "Owner"
            }

        // PROFILE IMAGE
        db.collection("posts")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { docs ->

                for (doc in docs) {

                    val url = doc.getString("business_pic")

                    if (!url.isNullOrEmpty()) {

                        Glide.with(binding.imgOwnerProfile)
                            .load(url)
                            .circleCrop()
                            .into(binding.imgOwnerProfile)

                        break
                    }
                }
            }

        // header
        binding.header.tvHeaderTitle.text = "Details"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        //direction and call
         latitude = intent.getDoubleExtra("LAT",0.0)
         longitude = intent.getDoubleExtra("LNG",0.0)
         phone = intent.getStringExtra("PHONE") ?: ""
        Log.d("PHONE_DEBUG", phone)

        binding.btnDirection.setOnClickListener {

            val uri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"

            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")

            startActivity(intent)
        }
        binding.btnCall.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:$phone")

            startActivity(intent)
        }
        //description
        val description = intent.getStringExtra("DESCRIPTION") ?: ""

        binding.txtDescription.text = description


        // rating bar logic
        var lastRating = 0

        binding.ratingBar.setOnTouchListener { view, event ->

            val ratingBar = view as android.widget.RatingBar

            val width = ratingBar.width
            val stars = ratingBar.numStars

            val touchedRating = (event.x / width * stars).toInt() + 1

            if (touchedRating == lastRating) {

                ratingBar.rating = 0f
                lastRating = 0

            } else {

                ratingBar.rating = touchedRating.toFloat()
                lastRating = touchedRating
            }

            true
        }


        setupDrawer()

    }

    private fun setupDrawer() {

        binding.studentDrawer.menuHome.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        }

        binding.studentDrawer.menuProfile.setOnClickListener {
            startActivity(Intent(this, StudentProfileActivity::class.java))
        }

        binding.studentDrawer.menuHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    // IMAGE SLIDER DOTS
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

    // ACTIVE DOT
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
}