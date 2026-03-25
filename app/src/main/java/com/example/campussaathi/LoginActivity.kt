package com.example.campussaathi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)

                if (account != null && account.idToken != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                } else {
                    toast("Google ID Token is null")
                }

            } catch (e: ApiException) {
                toast("Google Sign-In Failed: ${e.statusCode}")
            }
        } else {
            toast("Google Sign-In Cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("554123832675-or01siknqidq0scep6qu1crjmcc51i3j.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        val txtSignup = findViewById<TextView>(R.id.txtSignup)
        val txtForgot = findViewById<TextView>(R.id.txtForgotPassword)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkLocationAndNavigate()
            return
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please fill all fields")
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    checkLocationAndNavigate()
                }
                .addOnFailureListener {
                    stopLoading()
                    toast(it.message ?: "Login failed")
                }
        }

        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        txtSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        txtForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->

                val user = authResult.user

                if (authResult.additionalUserInfo?.isNewUser == true) {

                    val userData = hashMapOf(
                        "fullName" to (user?.displayName ?: ""),
                        "email" to (user?.email ?: ""),
                        "role" to "",
                        "isVerified" to false,
                        "createdAt" to com.google.firebase.Timestamp.now()
                    )

                    db.collection("users").document(user!!.uid).set(userData)
                        .addOnSuccessListener {
                            startActivity(Intent(this, SelectLocationActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            stopLoading()
                            toast("Failed to create user profile")
                        }

                } else {
                    startActivity(Intent(this, SelectLocationActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                stopLoading()
                toast("Google authentication failed")
            }
    }

    private fun checkLocationAndNavigate() {
        val prefs = getSharedPreferences("CampusSaathiPrefs", Context.MODE_PRIVATE)
        val city = prefs.getString("selected_city", null)
        val college = prefs.getString("selected_college", null)

        if (city == "Khamgaon" && college == "GPK") {
            val uid = auth.currentUser?.uid
            if (uid != null) checkUserRole(uid)
            else open(SelectLocationActivity::class.java)
        } else {
            open(SelectLocationActivity::class.java)
        }
    }

    private fun checkUserRole(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    open(RoleSelectionActivity::class.java)
                    return@addOnSuccessListener
                }

                val role = doc.getString("role")

                if (role.isNullOrEmpty()) {
                    open(RoleSelectionActivity::class.java)
                } else {
                    when (role) {
                        "admin" -> open(AdminActivityMain::class.java)
                        "student" -> open(StudentActivity::class.java)
                        "owner" -> open(OwnerMainActivity::class.java)
                        else -> open(RoleSelectionActivity::class.java)
                    }
                }
                stopLoading()
            }
            .addOnFailureListener {
                toast("Database error")
                stopLoading()
            }
    }

    private fun open(cls: Class<*>) {
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun stopLoading() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        progressBar.visibility = View.GONE
        btnLogin.isEnabled = true
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
