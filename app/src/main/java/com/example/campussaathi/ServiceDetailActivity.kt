package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.campussaathi.databinding.ActivityServiceDetailBinding
import androidx.viewpager2.widget.ViewPager2
import android.util.Log

class ServiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServiceDetailBinding

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var contact: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Get data from intent
        val serviceName = intent.getStringExtra("serviceName")
        val description = intent.getStringExtra("description")
        contact = intent.getStringExtra("contact") ?: ""

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        // Set data to UI
        binding.serviceName.text = serviceName
        binding.serviceDesc.text = description
        binding.serviceContact.text = "Contact: $contact"

        val photos = intent.getStringArrayListExtra("photos") ?: arrayListOf()
        Log.d("PHOTOS_DEBUG", photos.toString())

        if (photos.isNotEmpty()) {

            binding.imageSlider.adapter = ImageSliderAdapter(photos)

            // DOTS INDICATOR CODE START
            val dots = Array(photos.size) { ImageView(this) }

            for (i in dots.indices) {

                dots[i].setImageResource(R.drawable.dot_inactive)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                params.setMargins(8, 0, 8, 0)

                binding.dotsLayout.addView(dots[i], params)
            }

            dots[0].setImageResource(R.drawable.dot_active)

            binding.imageSlider.registerOnPageChangeCallback(
                object : ViewPager2.OnPageChangeCallback() {

                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)

                        for (i in dots.indices) {
                            dots[i].setImageResource(R.drawable.dot_inactive)
                        }

                        if (position < dots.size) {
                            dots[position].setImageResource(R.drawable.dot_active)
                        }
                    }
                }
            )
        }


        // Call button
        binding.btnCall.setOnClickListener {

            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$contact")
            startActivity(intent)
        }

        // Open Maps navigation
        binding.btnMap.setOnClickListener {

            val uri = Uri.parse("google.navigation:q=$latitude,$longitude")

            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
            mapIntent.setPackage("com.google.android.apps.maps")

            startActivity(mapIntent)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }




    }

    }
