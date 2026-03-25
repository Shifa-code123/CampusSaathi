package com.example.campussaathi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SelectLocationActivity : AppCompatActivity() {

    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerCollege: Spinner
    private lateinit var btnContinue: Button
    private lateinit var prefs: SharedPreferences

    private val cities = arrayOf("Select City", "Khamgaon", "Shegaon","Amravati","Buldana","Other")
    private val collegesKhamgaon = arrayOf("Select College", "Government Polytechnic Khamgaon (GPK)", "GS College", "ITI")
    private val collegesEmpty = arrayOf("Select College")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        prefs = getSharedPreferences("CampusSaathiPrefs", Context.MODE_PRIVATE)

        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerCollege = findViewById(R.id.spinnerCollege)
        btnContinue = findViewById(R.id.btnContinue)

        setupCitySpinner()

        btnContinue.setOnClickListener {
            validateAndProceed()
        }
    }

    private fun setupCitySpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = adapter

        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCity = cities[position]
                if (selectedCity == "Khamgaon") {
                    spinnerCollege.isEnabled = true
                    setupCollegeSpinner(collegesKhamgaon)
                } else {
                    spinnerCollege.isEnabled = false
                    setupCollegeSpinner(collegesEmpty)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCollegeSpinner(collegeList: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, collegeList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCollege.adapter = adapter
    }

    private fun validateAndProceed() {
        val selectedCity = spinnerCity.selectedItem.toString()
        val selectedCollege = spinnerCollege.selectedItem.toString()

        if (selectedCity == "Khamgaon" && selectedCollege == "Government Polytechnic Khamgaon (GPK)") {
            // Save to SharedPreferences
            prefs.edit().apply {
                putString("selected_city", "Khamgaon")
                putString("selected_college", "GPK")
                apply()
            }

            // Navigate to RoleSelectionActivity
            val intent = Intent(this, RoleSelectionActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Service not available for selected location", Toast.LENGTH_SHORT).show()
        }
    }
}
