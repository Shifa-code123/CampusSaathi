package com.example.campussaathi

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import com.example.campussaathi.databinding.ActivityStudentDashboardBinding


class StudentDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Header title change
        binding.layoutHeader.tvHeaderTitle.text = "Home"

        // Menu Click
        binding.layoutHeader.ivMenu.setOnClickListener {
            // Drawer open code
        }

        // Notification Click
        binding.layoutHeader.ivNotification.setOnClickListener {
            // Open notification screen
        }

        // Profile Click
        binding.layoutHeader.ivProfile.setOnClickListener {
            // Open profile screen
        }
        Log.d("CHECK_ACTIVITY", "StudentDashboardActivity OPENED")

         val home = findViewById<View>(R.id.cs_footer_home_container)
        val explore = findViewById<View>(R.id.cs_footer_explore_container)
        val nearme = findViewById<View>(R.id.cs_footer_nearme_container)
        val help = findViewById<View>(R.id.cs_footer_help_container)

        val homeIcon = findViewById<ImageView>(R.id.cs_footer_home_icon)
        val exploreIcon = findViewById<ImageView>(R.id.cs_footer_explore_icon)
        val nearmeIcon = findViewById<ImageView>(R.id.cs_footer_nearme_icon)
        val helpIcon = findViewById<ImageView>(R.id.cs_footer_help_icon)

        val homeText = findViewById<TextView>(R.id.cs_footer_home_text)
        val exploreText = findViewById<TextView>(R.id.cs_footer_explore_text)
        val nearmeText = findViewById<TextView>(R.id.cs_footer_nearme_text)
        val helpText = findViewById<TextView>(R.id.cs_footer_help_text)

        fun resetSelection() {

            val defaultColor = getColor(R.color.cs_footer_default)

            home.setBackgroundResource(0)
            explore.setBackgroundResource(0)
            nearme.setBackgroundResource(0)
            help.setBackgroundResource(0)

            homeIcon.setColorFilter(defaultColor)
            exploreIcon.setColorFilter(defaultColor)
            nearmeIcon.setColorFilter(defaultColor)
            helpIcon.setColorFilter(defaultColor)

            homeText.setTextColor(defaultColor)
            exploreText.setTextColor(defaultColor)
            nearmeText.setTextColor(defaultColor)
            helpText.setTextColor(defaultColor)
        }


        home.setOnClickListener {
            resetSelection()
            selectItem(home, homeIcon, homeText)
        }

        explore.setOnClickListener {
            resetSelection()
            selectItem(explore, exploreIcon, exploreText)
        }

        nearme.setOnClickListener {
            resetSelection()
            selectItem(nearme, nearmeIcon, nearmeText)
        }

        help.setOnClickListener {
            resetSelection()
            selectItem(help, helpIcon, helpText)
        }

// Default selected item
        selectItem(home, homeIcon, homeText)
    }
    private fun selectItem(container: View, icon: ImageView, text: TextView) {

        val selectedColor = getColor(R.color.cs_footer_selected_icon)

        container.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(150)
            .withEndAction {
                container.animate().scaleX(1f).scaleY(1f).duration = 100
            }

        container.setBackgroundResource(R.drawable.cs_footer_bg_selected)
        icon.setColorFilter(selectedColor)
        text.setTextColor(selectedColor)
    }
}

