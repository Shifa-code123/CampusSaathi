package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.campussaathi.databinding.FragmentStudentServiceDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class Student_ServiceDetailFragment : Fragment() {

    private var _binding: FragmentStudentServiceDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStudentServiceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val serviceId = arguments?.getString("serviceId") ?: return

        // Header handled by StudentActivity
        (activity as? StudentActivity)?.updateHeaderForFragment("Service Details", isBack = true)

        fetchServiceDetails(serviceId)
    }

    private fun fetchServiceDetails(serviceId: String) {
        FirebaseFirestore.getInstance().collection("services").document(serviceId)
            .get()
            .addOnSuccessListener { document ->
                val service = document.toObject(Student_ServiceModel::class.java)
                service?.let {
                    (activity as? StudentActivity)?.updateHeaderForFragment(it.serviceName, isBack = true)

                    binding.tvDetailName.text = it.serviceName
                    binding.tvDetailDesc.text = it.description
                    binding.tvDetailContact.text = it.contact
                    binding.tvDetailDistance.text = "${it.distance} from campus"

                    if (it.photos.isNotEmpty()) {
                        binding.vpImages.adapter = Student_ImageAdapter(it.photos)
                    }

                    binding.btnCall.setOnClickListener { _ ->
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:${it.contact}")
                        startActivity(intent)
                    }

                    binding.btnDirections.setOnClickListener { _ ->
                        if (it.latitude != null && it.longitude != null) {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${it.latitude},${it.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            startActivity(mapIntent)
                        } else {
                            Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}