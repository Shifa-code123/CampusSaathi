package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.campussaathi.databinding.ActivityHelpBinding
import com.example.campussaathi.databinding.ItemEmergencyCardBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Header Title
        binding.header.tvHeaderTitle.text = "Help"

        // Drawer Open
        binding.header.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Footer Setup
        setupFooter("help")

        setupDrawer()

        // Emergency Cards
        setupEmergencyCards()
    }



    // ---------------- FOOTER ----------------

    private fun setupFooter(selectedTab: String) {

        fun resetSelection() {
            val defaultColor = getColor(R.color.cs_footer_default)

            binding.csFooter.csFooterHomeContainer.setBackgroundResource(0)
            binding.csFooter.csFooterExploreContainer.setBackgroundResource(0)
            binding.csFooter.csFooterNearmeContainer.setBackgroundResource(0)
            binding.csFooter.csFooterHelpContainer.setBackgroundResource(0)

            binding.csFooter.csFooterHomeIcon.setColorFilter(defaultColor)
            binding.csFooter.csFooterExploreIcon.setColorFilter(defaultColor)
            binding.csFooter.csFooterNearmeIcon.setColorFilter(defaultColor)
            binding.csFooter.csFooterHelpIcon.setColorFilter(defaultColor)

            binding.csFooter.csFooterHomeText.setTextColor(defaultColor)
            binding.csFooter.csFooterExploreText.setTextColor(defaultColor)
            binding.csFooter.csFooterNearmeText.setTextColor(defaultColor)
            binding.csFooter.csFooterHelpText.setTextColor(defaultColor)
        }

        fun selectItem(container: View, icon: ImageView, text: TextView) {
            val selectedColor = getColor(R.color.cs_footer_selected_icon)

            container.setBackgroundResource(R.drawable.cs_footer_bg_selected)
            icon.setColorFilter(selectedColor)
            text.setTextColor(selectedColor)

            container.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(120)
                .withEndAction {
                    container.animate().scaleX(1f).scaleY(1f).duration = 80
                }
        }

        resetSelection()

        when (selectedTab) {
            "help" -> selectItem(
                binding.csFooter.csFooterHelpContainer,
                binding.csFooter.csFooterHelpIcon,
                binding.csFooter.csFooterHelpText
            )
        }

        binding.csFooter.csFooterHomeContainer.setOnClickListener {
            startActivity(Intent(this, StudentDashboardActivity::class.java))
        }

        binding.csFooter.csFooterExploreContainer.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        /*binding.csFooter.csFooterNearmeContainer.setOnClickListener {
            startActivity(Intent(this, NearMeActivity::class.java))
        }*/

        binding.csFooter.csFooterHelpContainer.setOnClickListener {
            // Already on Help
        }
    }

    private fun setupDrawer() {

        /*binding.studentDrawer.menuHome.setOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuSaved.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }*/

        binding.studentDrawer.menuHelp.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.studentDrawer.menuLogout.setOnClickListener {
            showLogoutDialog()
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun showLogoutDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to logout?")
        builder.setPositiveButton("Yes") { _, _ ->
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
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
}