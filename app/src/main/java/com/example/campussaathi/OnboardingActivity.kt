package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var dotsLayout: LinearLayout

    private lateinit var adapter: OnboardingAdapter
    private lateinit var onboardingItems: List<OnboardingItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        btnNext = findViewById(R.id.btnNext)
        dotsLayout = findViewById(R.id.dotsLayout)

        // 🔥 Data
        onboardingItems = listOf(
            OnboardingItem(
                "Find PG and Mess Easily",
                "Trusted services near your college with ratings and reviews",
                R.drawable.page1
            ),
            OnboardingItem(
                "Compare & Choose",
                "Check prices, facilities and reviews before booking",
                R.drawable.page2
            ),
            OnboardingItem(
                "Connect Instantly",
                "Contact owners directly and save your time",
                R.drawable.page3
            )
        )

        adapter = OnboardingAdapter(onboardingItems)
        viewPager.adapter = adapter

        setupDots(0)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                setupDots(position)

                if (position == onboardingItems.size - 1) {
                    btnNext.text = "Get Started"
                } else {
                    btnNext.text = "Next"
                }
            }
        })

        btnNext.setOnClickListener {
            if (viewPager.currentItem < onboardingItems.size - 1) {
                viewPager.currentItem += 1
            } else {
                // ✅ SAVE ONE TIME FLAG
                val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                sharedPref.edit().putBoolean("isFirstTime", false).apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    // 🔥 DOTS LOGIC
    private fun setupDots(position: Int) {
        dotsLayout.removeAllViews()

        for (i in onboardingItems.indices) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params

            if (i == position) {
                dot.setBackgroundResource(R.drawable.dot_active)
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive)
            }

            dotsLayout.addView(dot)
        }
    }
}