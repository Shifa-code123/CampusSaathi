package com.example.campussaathi

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ServicesFragment : Fragment(R.layout.fragment_services) {

    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerServices)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Temporary dummy adapter
        recyclerView.adapter = DummyServiceAdapter()
    }
}