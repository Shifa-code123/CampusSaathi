package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditServicesActivity : AppCompatActivity() {

    private lateinit var rvEditServices: RecyclerView
    private val serviceList = mutableListOf<Student_ServiceModel>()
    private lateinit var adapter: EditServiceListAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_services)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        rvEditServices = findViewById(R.id.rvEditServices)
        rvEditServices.layoutManager = LinearLayoutManager(this)
        adapter = EditServiceListAdapter(serviceList)
        rvEditServices.adapter = adapter

        fetchUserServices()
    }

    private fun fetchUserServices() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("services")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { query ->
                serviceList.clear()
                for (doc in query) {
                    val service = Student_ServiceModel(
                        id = doc.id,
                        serviceName = doc.getString("serviceName") ?: "",
                        category = doc.getString("category") ?: "",
                        description = doc.getString("description") ?: "",
                        status = doc.getString("status") ?: "pending"
                    )
                    serviceList.add(service)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show()
            }
    }
}