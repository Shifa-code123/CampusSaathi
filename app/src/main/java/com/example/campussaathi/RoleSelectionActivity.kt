package com.example.campussaathi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java

class RoleSelectionActivity : AppCompatActivity() {

    private var selectedRole: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val cardStudent = findViewById<LinearLayout>(R.id.cardStudent)
        val cardOwner = findViewById<LinearLayout>(R.id.cardOwner)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        cardStudent.setOnClickListener {
            selectedRole = "student"
            Toast.makeText(this, "Student selected", Toast.LENGTH_SHORT).show()
        }

        cardOwner.setOnClickListener {
            selectedRole = "owner"
            Toast.makeText(this, "Owner selected", Toast.LENGTH_SHORT).show()
        }

        btnContinue.setOnClickListener {

            if (selectedRole == null) {
                Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser!!.uid

            db.collection("users").document(uid)
                .update("role", selectedRole)
                .addOnSuccessListener {

                    if (selectedRole == "student") {
                        startActivity(Intent(this, StudentDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this, ChooseOwnerType::class.java))
                    }
                    finish()
                }
        }
    }
}
