package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.campussaathi.databinding.FragmentHelpBinding
import com.example.campussaathi.databinding.ItemEmergencyCardBinding
import com.example.campussaathi.utils.DrawerManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class HelpFragment : Fragment() {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        DrawerManager.setupDrawer(
            requireActivity(),
            binding.drawerLayout,
            binding.studentDrawer.root
        )

        binding.header.tvHeaderTitle.text = "Help"

        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }


        setupEmergencyCards()
    }


    // ---------------- LOGOUT ----------------

    private fun showLogoutDialog() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log out of your account?")
            .setPositiveButton("Log Out") { _, _ ->

                FirebaseAuth.getInstance().signOut()

                val intent = Intent(requireContext(), LoginActivity::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------------- EMERGENCY CARDS ----------------

    private fun setupEmergencyCards() {

        val policeBinding = ItemEmergencyCardBinding.bind(binding.cardPolice.root)
        policeBinding.txtTitle.text = "Police"
        policeBinding.txtNumber.text = "100"
        policeBinding.imgIcon.setImageResource(R.drawable.ic_police)
        policeBinding.btnCall.setOnClickListener {
            openDialer("100")
        }

        val medicalBinding = ItemEmergencyCardBinding.bind(binding.cardMedical.root)
        medicalBinding.txtTitle.text = "Medical Emergency"
        medicalBinding.txtNumber.text = "108"
        medicalBinding.imgIcon.setImageResource(R.drawable.ic_medical)
        medicalBinding.btnCall.setOnClickListener {
            openDialer("108")
        }

        val ambulanceBinding = ItemEmergencyCardBinding.bind(binding.cardAmbulance.root)
        ambulanceBinding.txtTitle.text = "Ambulance"
        ambulanceBinding.txtNumber.text = "102"
        ambulanceBinding.imgIcon.setImageResource(R.drawable.ic_ambulance)
        ambulanceBinding.btnCall.setOnClickListener {
            openDialer("102")
        }
    }

    private fun openDialer(number: String) {

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}