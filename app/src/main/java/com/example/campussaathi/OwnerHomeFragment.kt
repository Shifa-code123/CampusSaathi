package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerHomeFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_owner_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        profileImage = view.findViewById(R.id.profileImage)

//        val btnCityHelp = view.findViewById<Button>(R.id.btnCityHelp)
        val txtOwnerName = view.findViewById<TextView>(R.id.txtOwnerName)
        val txtOwnerType = view.findViewById<TextView>(R.id.txtOwnerType)

//        btnCityHelp.setOnClickListener {
//            startActivity(Intent(requireContext(), ActivityCityHelp::class.java))
//        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        uid?.let {
            db.collection("owner_verifications").document(it).get()
                .addOnSuccessListener { doc ->
                    txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                    txtOwnerType.text = doc.getString("ownerType") ?: "Owner"
                }
        }

        loadProfileImage()
        setupButtons(view)
    }

    private fun setupButtons(view: View) {
        val nav = activity as? NavigationHandler

        view.findViewById<TextView>(R.id.txtBusinessProfile).setOnClickListener {
            nav?.navigateTo(R.id.nav_profile)
        }

        view.findViewById<Button>(R.id.btnViewListing).setOnClickListener {
            nav?.navigateTo(R.id.nav_services)
        }

        view.findViewById<Button>(R.id.btnEditListing).setOnClickListener {
            startActivity(Intent(requireContext(), EditServicesActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnAddListing).setOnClickListener {
            nav?.navigateTo(R.id.nav_add)
        }
    }

    private fun loadProfileImage() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val base64 = doc.getString("profileImageBase64")
                try {
                    if (!base64.isNullOrEmpty()) {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        profileImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {}
            }
    }
}