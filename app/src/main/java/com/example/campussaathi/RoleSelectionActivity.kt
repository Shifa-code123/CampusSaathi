package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RoleSelectionActivity : AppCompatActivity() {

    private var selectedRole: String = ""

    private lateinit var cardStudent: LinearLayout
    private lateinit var cardOwner: LinearLayout
    private lateinit var btnContinue: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        // UI references
        cardStudent = findViewById(R.id.cardStudent)
        cardOwner = findViewById(R.id.cardOwner)
        btnContinue = findViewById(R.id.btnContinue)

        // Default state (nothing selected)
        resetSelectionUI()

        // Student card click
        cardStudent.setOnClickListener {
            selectedRole = "student"
            highlightStudent()
            Toast.makeText(this, "Student selected", Toast.LENGTH_SHORT).show()
        }

        // Owner card click
        cardOwner.setOnClickListener {
            selectedRole = "owner"
            highlightOwner()
            Toast.makeText(this, "Owner selected", Toast.LENGTH_SHORT).show()
        }

        // Continue button
        btnContinue.setOnClickListener {

            if (selectedRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser!!.uid

            // Save role to Firestore (ONLY ONCE)
            db.collection("users").document(uid)
                .update("role", selectedRole)
                .addOnSuccessListener {

                    if (selectedRole == "student") {
                        startActivity(
                            Intent(this, StudentActivity::class.java)
                        )
                    } else {
                        startActivity(
                            Intent(this, ChooseOwnerType::class.java)
                        )
                    }
                    finish()
                }
        }
    }

    // ---------- UI helper functions ----------

    private fun resetSelectionUI() {
        cardStudent.setBackgroundResource(R.drawable.bg_role_unselected)
        cardOwner.setBackgroundResource(R.drawable.bg_role_unselected)
    }

    private fun highlightStudent() {
        cardStudent.setBackgroundResource(R.drawable.bg_role_selected)
        cardOwner.setBackgroundResource(R.drawable.bg_role_unselected)
    }

    private fun highlightOwner() {
        cardOwner.setBackgroundResource(R.drawable.bg_role_selected)
        cardStudent.setBackgroundResource(R.drawable.bg_role_unselected)
    }
}
