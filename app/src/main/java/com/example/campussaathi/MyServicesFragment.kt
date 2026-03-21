package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyServicesFragment : Fragment() {

    private lateinit var rvMyServices: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtNoServices: TextView
    private lateinit var adapter: MyServicesAdapter
    private val serviceList = mutableListOf<Student_ServiceModel>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_services, container, false)

        rvMyServices = view.findViewById(R.id.rvMyServices)
        progressBar = view.findViewById(R.id.progressBar)
        txtNoServices = view.findViewById(R.id.txtNoServices)

        rvMyServices.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyServicesAdapter(serviceList)
        rvMyServices.adapter = adapter

        fetchMyServices()

        return view
    }

    private fun fetchMyServices() {
        val uid = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE
        txtNoServices.visibility = View.GONE

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
                        contact = doc.getString("contact") ?: "",
                        latitude = doc.getDouble("latitude"),
                        longitude = doc.getDouble("longitude"),
                        photos = doc.get("photos") as? List<String> ?: emptyList(),
                        distance = doc.get("distanceFromCampus")?.toString()?.let { "$it km" } ?: "0.0 km",
                        status = doc.getString("status") ?: "pending"
                    )
                    serviceList.add(service)
                }

                progressBar.visibility = View.GONE
                if (serviceList.isEmpty()) {
                    txtNoServices.visibility = View.VISIBLE
                } else {
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}