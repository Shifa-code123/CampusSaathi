package com.example.campussaathi

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.ActivityCategoryServicesBinding
import com.google.firebase.firestore.FirebaseFirestore

class CategoryServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryServicesBinding
    private lateinit var adapter: ServicesAdapter
    private val list = ArrayList<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedCategory = intent.getStringExtra("categoryName") ?: ""

        Log.d("DEBUG", "Selected: $selectedCategory")

        // Header
        binding.header.tvHeaderTitle.text = selectedCategory
        binding.header.ivMenu.setImageResource(R.drawable.ic_back)
        binding.header.ivMenu.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Recycler
        binding.rvServices.layoutManager = LinearLayoutManager(this)
        adapter = ServicesAdapter(list)
        binding.rvServices.adapter = adapter

        fetchServices(selectedCategory)
    }

    private fun fetchServices(selectedCategory: String) {

        FirebaseFirestore.getInstance()
            .collection("services")
            .whereEqualTo("category", selectedCategory)   // 🔥 DIRECT FILTER
            .get()
            .addOnSuccessListener { result ->

                list.clear()

                for (doc in result) {

                    val service = Service(
                        serviceId = doc.id,
                        ownerId = doc.getString("ownerId") ?: "",
                        photos = doc.get("photos") as? List<String> ?: emptyList(),
                        serviceName = doc.getString("serviceName") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0,
                        phone = doc.getString("contact") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: ""
                    )

                    list.add(service)
                }

                adapter.notifyDataSetChanged()

                // EMPTY STATE
                if (list.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvServices.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvServices.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvServices.visibility = View.GONE
            }
    }
}