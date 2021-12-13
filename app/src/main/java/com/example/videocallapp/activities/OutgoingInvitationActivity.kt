package com.example.videocallapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videocallapp.R
import com.example.videocallapp.models.User
import com.example.videocallapp.network.ApiClient
import com.example.videocallapp.network.ApiService
import com.example.videocallapp.utilities.Constants
import com.example.videocallapp.utilities.PreferenceManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_outgoing_invitation.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*

class OutgoingInvitationActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private var inviterToken: String ?= null
    private var meetingRoom: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_outgoing_invitation)

        preferenceManager = PreferenceManager(applicationContext)


        // Show video icon
        val meetingType : String? = intent.getStringExtra("type")
        if (meetingType != null){
            if (meetingType == "video"){ imageMeetingTypeOutgoing.setImageResource(R.drawable.ic_video) }
        }

        val user : User = intent.getSerializableExtra("user") as User
        textFirstCharOutgoing.text = user.firstName.substring(0,1)
        textUsernameOutgoing.text = String.format("%s %s ", user.firstName,user.lastName)
        textEmailOutgoing.text = user.email

        imageStopInvivation.setOnClickListener {
            cancelInvitation(user.token)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                inviterToken = task.result
                if (meetingType != null){ initiateMeeting(meetingType,user.token) }
            }
        }


    }

    private fun initiateMeeting(meetingType : String, receiverToken : String){
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION)
            data.put(Constants.REMOTE_MSG_MEETING_TYPE,meetingType)
            data.put(Constants.KEY_FIRST_NAME,preferenceManager.getString(Constants.KEY_FIRST_NAME))
            data.put(Constants.KEY_LAST_NAME,preferenceManager.getString(Constants.KEY_LAST_NAME))
            data.put(Constants.KEY_EMAIL,preferenceManager.getString(Constants.KEY_EMAIL))
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN,inviterToken)

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" + UUID.randomUUID().toString().substring(0,5)
            data.put(Constants.REMOTE_MSG_MEETING_ROOM,meetingRoom)

            body.put(Constants.REMOTE_MSG_DATA,data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens)

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION)

        }catch (e : Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun sendRemoteMessage(remoteMessageBody: String, type: String) {
        val apiClient = ApiClient().getClient()
        apiClient!!.create(ApiService::class.java).sendRemoteMessage(
            Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful){
                    if (type == Constants.REMOTE_MSG_INVITATION){
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation sent successfully", Toast.LENGTH_SHORT).show()
                    } else if(type == Constants.REMOTE_MSG_INVITATION_RESPONSE){
                        Toast.makeText(this@OutgoingInvitationActivity, "Invitation Cancelled", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else{
                    Toast.makeText(this@OutgoingInvitationActivity, response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@OutgoingInvitationActivity, t.message, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun cancelInvitation(receiverTokens: String){
        try {
            val tokens = JSONArray()
            tokens.put(receiverTokens)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,Constants.REMOTE_MSG_INVITATION_CANCELLED)

            body.put(Constants.REMOTE_MSG_DATA,data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens)

            sendRemoteMessage(body.toString(),Constants.REMOTE_MSG_INVITATION_RESPONSE)

        }catch (e:Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private val invitationResponseReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context , intent: Intent) {
            val type : String? = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            type?.let {
                if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED) {
                    //Jitsi Meet connect
                    try {
                        val serverURL = URL("https://meet.jit.si")
                        val conferenceOptions = JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setWelcomePageEnabled(false)
                            .setRoom(meetingRoom)
                            .build()
                        JitsiMeetActivity.launch(this@OutgoingInvitationActivity,conferenceOptions)
                        finish()
                    }catch (e : Exception){
                        Toast.makeText(this@OutgoingInvitationActivity, e.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else if(type == Constants.REMOTE_MSG_INVITATION_REJECTED){
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(invitationResponseReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(invitationResponseReceiver)
    }

}