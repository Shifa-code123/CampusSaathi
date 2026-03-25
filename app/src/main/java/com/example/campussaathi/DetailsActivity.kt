package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ActivityDetailsBinding
import com.example.campussaathi.utils.DrawerManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.net.Uri
import android.view.View
import com.example.campussaathi.utils.ProfileImageLoader
import android.graphics.Color

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var photos: ArrayList<String>

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var phone: String = ""

    private lateinit var ownerId: String
    private lateinit var serviceName: String
    private lateinit var serviceId: String

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProfileImageLoader.loadProfile(binding.header.ivProfile)

        // Header
        binding.header.ivMenu.setImageResource(R.drawable.ic_back)
        binding.header.ivMenu.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        DrawerManager.setupDrawer(
            this,
            binding.drawerLayout,
            binding.studentDrawer.root
        )
        val drawerImage = binding.studentDrawer.root
            .findViewById<ImageView>(R.id.profileImage)

        ProfileImageLoader.loadProfile(drawerImage)

        binding.header.tvHeaderTitle.text = "Details"

        // Intent Data
        photos = intent.getStringArrayListExtra("PHOTOS") ?: ArrayList()
        serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""
        ownerId = intent.getStringExtra("OWNER_ID") ?: ""
        serviceId = intent.getStringExtra("SERVICE_ID") ?: ""
        latitude = intent.getDoubleExtra("LAT", 0.0)
        longitude = intent.getDoubleExtra("LNG", 0.0)
        phone = intent.getStringExtra("PHONE") ?: ""
        val description = intent.getStringExtra("DESCRIPTION") ?: ""

        binding.txtServiceName.text = serviceName
        binding.txtDescription.text = description

        // Owner name
        db.collection("owner_verifications")
            .document(ownerId)
            .get()
            .addOnSuccessListener {
                binding.txtOwnerName.text = it.getString("fullName") ?: "Owner"
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
                        Glide.with(this)
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
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        }

        // Call
        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            startActivity(intent)
        }

        setupActionButtons()
    }

    private fun setupActionButtons() {
        val userId = auth.currentUser?.uid ?: return

        // --- LIKE ---
        val userLikeRef = db.collection("likes").document(serviceId)
            .collection("userLikes").document(userId)

        userLikeRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                binding.imgDetailLike.setColorFilter(Color.RED)
            } else {
                binding.imgDetailLike.setColorFilter(Color.DKGRAY)
            }
        }

        binding.btnDetailLike.setOnClickListener {
            userLikeRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userLikeRef.delete()
                } else {
                    userLikeRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                }
            }
        }

        // --- COMMENT ---
        binding.btnDetailComment.setOnClickListener {
            val sheet = CommentBottomSheet(serviceId)
            sheet.show(supportFragmentManager, "comments")
        }

        // --- SAVE ---
        val saveRef = db.collection("saved").document(userId)
            .collection("services").document(serviceId)

        saveRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                binding.imgDetailSave.setImageResource(R.drawable.ic_saved)
            } else {
                binding.imgDetailSave.setImageResource(R.drawable.ic_cs_save)
            }
        }

        binding.btnDetailSave.setOnClickListener {
            saveRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    saveRef.delete()
                } else {
                    val data = hashMapOf(
                        "serviceId" to serviceId,
                        "serviceName" to serviceName,
                        "ownerId" to ownerId,
                        "photos" to photos,
                        "savedAt" to System.currentTimeMillis()
                    )
                    saveRef.set(data)
                }
            }
        }

        // --- RATE ---
        binding.btnDetailRate.setOnClickListener {
            val intent = Intent(this, RatingActivity::class.java)
            intent.putExtra("SERVICE_ID", serviceId)
            startActivity(intent)
        }
    }

    // ---------------- DOTS ----------------

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
            dot.setImageResource(
                if (i == position) R.drawable.active_dot
                else R.drawable.inactive_dot
            )
        }
    }
}