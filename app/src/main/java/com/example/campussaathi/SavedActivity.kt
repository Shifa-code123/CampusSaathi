package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.ActivitySavedBinding
import com.example.campussaathi.utils.DrawerManager
import com.example.campussaathi.utils.ProfileImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SavedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedBinding
    private lateinit var adapter: ServicesAdapter
    private val list = ArrayList<Service>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProfileImageLoader.loadProfile(binding.header.ivProfile)

        DrawerManager.setupDrawer(
            this,
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        // Drawer image
        val drawerImage = binding.studentDrawer.root
            .findViewById<ImageView>(R.id.profileImage)

        ProfileImageLoader.loadProfile(drawerImage)

        // 🔹 Header title
        binding.header.tvHeaderTitle.text = "Saved Services"

        // 🔹 Back button instead of menu
        binding.header.ivMenu.setImageResource(R.drawable.ic_back)
        binding.header.ivMenu.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 🔹 Disable drawer swipe (optional but clean)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // 🔹 Recycler
        binding.recyclerSaved.layoutManager = LinearLayoutManager(this)
        adapter = ServicesAdapter(list)
        binding.recyclerSaved.adapter = adapter

        // 🔹 Load data
        loadSaved()
    }

    private fun loadSaved() {

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("saved")
            .document(userId)
            .collection("services")
            .addSnapshotListener { value, _ ->

                list.clear()

                if (value != null) {
                    for (doc in value.documents) {

                        val service = Service(
                            serviceId = doc.getString("serviceId") ?: "",
                            ownerId = doc.getString("ownerId") ?: "",
                            photos = doc.get("photos") as? List<String> ?: emptyList(),
                            serviceName = doc.getString("serviceName") ?: "",
                            latitude = 0.0,
                            longitude = 0.0,
                            phone = "",
                            description = "",
                            category = doc.getString("category") ?: ""
                        )

                        list.add(service)
                    }
                }

                adapter.notifyDataSetChanged()

                // ✅ EMPTY STATE FIX
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.recyclerSaved.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.recyclerSaved.visibility = View.VISIBLE
                }
            }
    }
}