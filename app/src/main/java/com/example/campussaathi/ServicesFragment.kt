package com.example.campussaathi

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ServiceNameAdapter
    private val serviceList = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerServices)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ServiceNameAdapter(serviceList)
        recyclerView.adapter = adapter

        loadServices()
    }

    private fun loadServices() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("services") // ⚠️ same hona chahiye
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { result ->

                serviceList.clear()

                for (doc in result) {
                    val name = doc.getString("serviceName") ?: "No Name"
                    serviceList.add(name)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}