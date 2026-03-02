package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.campussaathi.databinding.ActivityHelpBinding
import com.example.campussaathi.databinding.ItemEmergencyCardBinding
import androidx.core.view.GravityCompat
import kotlin.jvm.java

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // ✅ Change Header Title Here
        binding.Header.tvHeaderTitle.text = "Help"

        binding.Header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        val home = findViewById<View>(R.id.cs_footer_home_container)
        val explore = findViewById<View>(R.id.cs_footer_explore_container)
        val nearme = findViewById<View>(R.id.cs_footer_nearme_container)
        val help = findViewById<View>(R.id.cs_footer_help_container)

        home.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
            finish()
        }

        /*explore.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
            finish()
        }

        nearme.setOnClickListener {
            startActivity(Intent(this, NearMeActivity::class.java))
            finish()
        }*/

        help.setOnClickListener {
            // Already on help page
        }


        setupEmergencyCards()
    }


    private fun setupEmergencyCards() {

        // Police
        val policeBinding = ItemEmergencyCardBinding.bind(binding.cardPolice.root)
        policeBinding.txtTitle.text = "Police"
        policeBinding.txtNumber.text = "100"
        policeBinding.imgIcon.setImageResource(R.drawable.ic_police)
        policeBinding.btnCall.setOnClickListener {
            openDialer("100")
        }

        // Medical
        val medicalBinding = ItemEmergencyCardBinding.bind(binding.cardMedical.root)
        medicalBinding.txtTitle.text = "Medical Emergency"
        medicalBinding.txtNumber.text = "108"
        medicalBinding.imgIcon.setImageResource(R.drawable.ic_medical)
        medicalBinding.btnCall.setOnClickListener {
            openDialer("108")
        }

        // Ambulance
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
}