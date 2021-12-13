package com.example.videocallapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.videocallapp.R
import com.example.videocallapp.utilities.Constants
import com.example.videocallapp.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_in.*



class SignInActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        preferenceManager = PreferenceManager(applicationContext)
        // The account will remain open until the sign-out button is clicked
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            val intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        textSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        buttonSıgnIn.setOnClickListener {
            if (inputEmailIn.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter mail", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmailIn.text.toString()).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show()
            } else if (inputPasswordIn.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
            } else {
                signIn()
            }
        }

    }

    private fun signIn() {
        buttonSıgnIn.isVisible = false
        signInProgressBar.isVisible = true

        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, inputEmailIn.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, inputPasswordIn.text.toString())
            .get()
            .addOnCompleteListener {
                it.let {
                    if (it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                        val documentSnapshot = it.result!!.documents[0]
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_ID,documentSnapshot.id)
                        preferenceManager.putString(Constants.KEY_FIRST_NAME, documentSnapshot.getString(Constants.KEY_FIRST_NAME)!!)
                        preferenceManager.putString(Constants.KEY_LAST_NAME, documentSnapshot.getString(Constants.KEY_LAST_NAME)!!)
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL)!!)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        signInProgressBar.isVisible = false
                        buttonSıgnIn.isVisible = true
                        Toast.makeText(this, "Unable to sign in", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}