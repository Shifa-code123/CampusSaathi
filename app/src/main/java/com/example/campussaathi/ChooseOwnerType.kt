package com.example.campussaathi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChooseOwnerType : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Stores selected owner type
    private var selectedOwnerType: String = ""

    // Cards
    private lateinit var cardRoomPg: CardView
    private lateinit var cardMess: CardView
    private lateinit var cardTution: CardView
    private lateinit var cardService: CardView
    private lateinit var cardCity: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_owner_type)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Bind cards (MATCH XML IDS)
        cardRoomPg = findViewById(R.id.cardRoomPg)
        cardMess = findViewById(R.id.cardMess)
        cardTution = findViewById(R.id.cardTution)
        cardService = findViewById(R.id.cardService)
        cardCity = findViewById(R.id.cardCity)

        val btnContinue = findViewById<Button>(R.id.btnContinue)

        resetHighlight()

        // -------- CARD CLICKS --------

        cardRoomPg.setOnClickListener {
            selectedOwnerType = "room_pg"
            highlight(cardRoomPg)
            toast("Room / PG selected")
        }

        cardMess.setOnClickListener {
            selectedOwnerType = "mess"
            highlight(cardMess)
            toast("Mess selected")
        }

        cardTution.setOnClickListener {
            selectedOwnerType = "tuition"
            highlight(cardTution)
            toast("Tuition selected")
        }

        cardService.setOnClickListener {
            selectedOwnerType = "service"
            highlight(cardService)
            toast("Service selected")
        }

        cardCity.setOnClickListener {
            selectedOwnerType = "city"
            highlight(cardCity)
            toast("City service selected")
        }

        // -------- CONTINUE --------

        btnContinue.setOnClickListener {

            if (selectedOwnerType.isEmpty()) {
                toast("Please select an owner type")
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid
            if (uid == null) {
                toast("User not logged in")
                return@setOnClickListener
            }

            val updateData = hashMapOf(
                "ownerType" to selectedOwnerType,
                "ownerSetupStep" to 1,
                "ownerSetupDone" to false,
                "isVerified" to false
            )

            db.collection("users").document(uid)
                .update(updateData as Map<String, Any>)
                .addOnSuccessListener {

                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            toast("Owner type saved! Verification email sent.")
                        }

                    // CONDITION BASED REDIRECT
                    if (selectedOwnerType == "service") {

                        startActivity(
                            Intent(this, ActivityOwnerChooseTypeService::class.java)
                        )

                    } else {

                        startActivity(
                            Intent(this, OwnerVerification::class.java)
                        )
                    }

                    finish()
                }
                .addOnFailureListener {
                    toast("Failed to save owner type")
                }
        }
    }

    // -------- UI HELPERS --------

    private fun resetHighlight() {
        cardRoomPg.setBackgroundResource(R.drawable.bg_role_unselected)
        cardMess.setBackgroundResource(R.drawable.bg_role_unselected)
        cardTution.setBackgroundResource(R.drawable.bg_role_unselected)
        cardService.setBackgroundResource(R.drawable.bg_role_unselected)
        cardCity.setBackgroundResource(R.drawable.bg_role_unselected)
    }

    private fun highlight(selected: CardView) {
        resetHighlight()
        selected.setBackgroundResource(R.drawable.bg_role_selected)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
