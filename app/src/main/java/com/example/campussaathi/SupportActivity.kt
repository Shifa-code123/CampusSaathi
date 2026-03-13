package com.example.campussaathi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton

class SupportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        val faq1 = findViewById<LinearLayout>(R.id.faq1)
        val faq2 = findViewById<LinearLayout>(R.id.faq2)
        val faq3 = findViewById<LinearLayout>(R.id.faq3)
        val faq4 = findViewById<LinearLayout>(R.id.faq4)

        val ans1 = findViewById<MaterialCardView>(R.id.ans1)
        val ans2 = findViewById<MaterialCardView>(R.id.ans2)
        val ans3 = findViewById<MaterialCardView>(R.id.ans3)
        val ans4 = findViewById<MaterialCardView>(R.id.ans4)

        faq1.setOnClickListener { toggle(ans1) }
        faq2.setOnClickListener { toggle(ans2) }
        faq3.setOnClickListener { toggle(ans3) }
        faq4.setOnClickListener { toggle(ans4) }

        val report = findViewById<MaterialButton>(R.id.btnReport)
        val email = findViewById<MaterialButton>(R.id.btnEmail)
        val whatsapp = findViewById<MaterialButton>(R.id.btnWhatsapp)
        val website = findViewById<MaterialButton>(R.id.btnWebsite)


        report.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Report Problem")
                .setMessage("You can report issues directly to our support team.")
                .setPositiveButton("Email Report") { _, _ ->

                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:support@campussaathi.com")
                    startActivity(intent)

                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        email.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Email Support")
                .setMessage("support@campussaathi.com")
                .setPositiveButton("Send Email") { _, _ ->

                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:support@campussaathi.com")
                    startActivity(intent)

                }
                .show()
        }

        whatsapp.setOnClickListener {

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/919999999999")
            startActivity(intent)

        }

        website.setOnClickListener {

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://campussaathi.com")
            startActivity(intent)

        }
    }

    private fun toggle(card: MaterialCardView) {

        if (card.visibility == View.GONE) {
            card.visibility = View.VISIBLE
        } else {
            card.visibility = View.GONE
        }

    }
}