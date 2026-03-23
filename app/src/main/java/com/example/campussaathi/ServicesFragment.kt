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
    private val serviceIds = mutableListOf<String>()
    private var ownerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ownerId = arguments?.getString("ownerId") ?: FirebaseAuth.getInstance().currentUser?.uid
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerServices)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ServiceNameAdapter(serviceList, serviceIds)
        recyclerView.adapter = adapter

        loadServices()
    }

    private fun loadServices() {
        val uid = ownerId ?: return

        FirebaseFirestore.getInstance()
            .collection("services")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { result ->
                serviceList.clear()
                serviceIds.clear()
                for (doc in result) {
                    val name = doc.getString("serviceName") ?: "No Name"
                    serviceList.add(name)
                    serviceIds.add(doc.id)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    companion object {
        fun newInstance(ownerId: String): ServicesFragment {
            val fragment = ServicesFragment()
            val args = Bundle()
            args.putString("ownerId", ownerId)
            fragment.arguments = args
            return fragment
        }
    }
}