package com.example.videocallapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videocallapp.R
import com.example.videocallapp.adapters.UsersAdapter
import com.example.videocallapp.listeners.UsersListener
import com.example.videocallapp.models.User
import com.example.videocallapp.utilities.Constants
import com.example.videocallapp.utilities.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),  UsersListener{

    private lateinit var preferenceManager: PreferenceManager
    private var users = ArrayList<User>()
    private lateinit var usersAdapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferenceManager = PreferenceManager(applicationContext)

        // Show user first name and last name in toolbar
        textTitle.text = String.format("%s %s",
            preferenceManager.getString(Constants.KEY_FIRST_NAME),
            preferenceManager.getString(Constants.KEY_LAST_NAME)
        )
        textSignOut.setOnClickListener { signOut() }

        // Send FCM token database
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) { sendFCMTokenDatabase(task.result) }
        }

        //Activity is attached to a RecyclerView
        usersAdapter = UsersAdapter(users, this)
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = usersAdapter

        swipeRefreshLayout.setOnRefreshListener(this::getUsers)

        getUsers()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getUsers() {
        swipeRefreshLayout.isRefreshing = true
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {
                swipeRefreshLayout.isRefreshing = false
                val myUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if (it.isSuccessful && it.result != null) {
                    // Using swipe refresh layout, it can be called multiple times that's why I need to clear user list before adding new data
                    users.clear()
                    //here, I will display the user list except for the currently signed-in user,
                    // because no one will have a meeting with himself. That's why I am excluding a signed-in user from the list
                    for (documentSnapshot: QueryDocumentSnapshot in it.result) {
                        if (myUserId.equals(documentSnapshot.id)) {
                            continue
                        } else{
                            val user = User()
                            user.firstName = documentSnapshot.getString(Constants.KEY_FIRST_NAME).toString()
                            user.lastName = documentSnapshot.getString(Constants.KEY_LAST_NAME).toString()
                            user.email = documentSnapshot.getString(Constants.KEY_EMAIL).toString()
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                            users.add(user)
                        }
                    }
                    if (users.isNotEmpty()) {
                        usersAdapter.notifyDataSetChanged()
                    } else {
                        textErrorMessage.text = String.format("%s", " Empty: No users available")
                        textErrorMessage.isVisible = true
                    }
                } else {
                    textErrorMessage.text = String.format("%s", "No users available")
                    textErrorMessage.isVisible = true
                }
            }
    }

    private fun sendFCMTokenDatabase(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                Toast.makeText(this, "Unable to send token: " + it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOut() {
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show()
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID)!!)
        val updates: HashMap<String, Any> = HashMap()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clearPreference()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Unable to sign out", Toast.LENGTH_SHORT).show()
            }
    }

    override fun initiateVideoMeeting(user: User) {
        if (user.token == "null" || user.token.trim().isEmpty() ){
            Toast.makeText(this, user.firstName+ " " + user.lastName + " is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
            val intent = Intent(applicationContext,OutgoingInvitationActivity::class.java)
            intent.putExtra("user", user)
            intent.putExtra("type","video")
            startActivity(intent)
        }
    }

    override fun initiateAudioMeeting(user: User) {
        if (user.token == "null" || user.token.trim().isEmpty()){
            Toast.makeText(this, user.firstName+ " " + user.lastName + " is not available for meeting", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Audio meeting with " + user.firstName + " " + user.lastName, Toast.LENGTH_SHORT).show()
            Log.d("bk","user.token: " + user.token)
        }
    }

}