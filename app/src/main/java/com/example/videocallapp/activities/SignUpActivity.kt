package com.example.videocallapp.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.videocallapp.R
import com.example.videocallapp.utilities.Constants
import com.example.videocallapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        imageBack.setOnClickListener { onBackPressed() }
        textSignIn.setOnClickListener { onBackPressed() }

        preferenceManager = PreferenceManager(applicationContext)

        buttonSıgnUp.setOnClickListener {
            if (inputFirstName.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter first name", Toast.LENGTH_SHORT).show()
            } else if (inputLastName.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter last name", Toast.LENGTH_SHORT).show()
            } else if (inputEmail.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.text.toString()).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            } else if (inputPassword.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show()
            } else if (inputConfirmPassword.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT).show()
            } else if (inputPassword.text.toString() != inputConfirmPassword.text.toString()) {
                Toast.makeText(this, "Password & confirm password must be", Toast.LENGTH_SHORT).show()
            } else {
                signUp()
            }
        }
    }

    private fun signUp() {
        buttonSıgnUp.isVisible = false
        signUpProgressBar.isVisible = true

        val database: FirebaseFirestore = FirebaseFirestore.getInstance()
        val user: HashMap<String, Any> = HashMap()
        user[Constants.KEY_FIRST_NAME] = inputFirstName.text.toString()
        user[Constants.KEY_LAST_NAME] = inputLastName.text.toString()
        user[Constants.KEY_EMAIL] = inputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = inputPassword.text.toString()

        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, it.id)
                preferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirstName.text.toString())
                preferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.text.toString())
                preferenceManager.putString(Constants.KEY_EMAIL, inputEmail.text.toString())
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }.addOnFailureListener {
                signUpProgressBar.isVisible = false
                buttonSıgnUp.isVisible = true
                Toast.makeText(this, "Error: " + it.message, Toast.LENGTH_SHORT).show()
            }

    }
}