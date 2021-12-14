package com.example.videocallapp.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videocallapp.R
import com.example.videocallapp.network.ApiClient
import com.example.videocallapp.network.ApiService
import com.example.videocallapp.utilities.Constants
import kotlinx.android.synthetic.main.activity_incoming_invivation.*
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

class IncomingInvivationActivity : AppCompatActivity() {

    private var meetingType: String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incoming_invivation)

        meetingType = intent.getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE)

        // Show icon
        if (meetingType != null){
            if (meetingType == "video"){
                imageMeetingType.setImageResource(R.drawable.ic_video)
            }else{
                imageMeetingType.setImageResource(R.drawable.ic_audio)
            }
        }

        val firstName = intent.getStringExtra(Constants.KEY_FIRST_NAME)
        if (firstName != null) { textFirstChar.text = firstName.substring(0, 1) }

        textUsername.text = String.format("%s %s ", firstName, intent.getStringExtra(Constants.KEY_LAST_NAME))
        textEmail.text = intent.getStringExtra(Constants.KEY_EMAIL)

        imageAcceptInvitation.setOnClickListener {
            sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED,intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN).toString())
        }
        imageRejectInvitation.setOnClickListener {
            sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED,intent.getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN).toString())
        }

    }

    private fun sendInvitationResponse(typeIncoming: String, receiverTokens: String){
        try {
            val tokens = JSONArray()
            tokens.put(receiverTokens)

            val body = JSONObject()
            val data = JSONObject()

            data.put(Constants.REMOTE_MSG_TYPE,Constants.REMOTE_MSG_INVITATION_RESPONSE)
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE,typeIncoming)

            body.put(Constants.REMOTE_MSG_DATA,data)
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens)

            sendRemoteMessage(body.toString(),typeIncoming)

        }catch (e:Exception){
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
                    if (type == Constants.REMOTE_MSG_INVITATION_ACCEPTED){
                        try {
                            //Jitsi Meet connect
                            val serverURL = URL("https://meet.jit.si")
                            val builder = JitsiMeetConferenceOptions.Builder()
                                .setServerURL(serverURL)
                                .setWelcomePageEnabled(false)
                                .setRoom(intent.getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                            if (meetingType == "audio"){
                                builder.setVideoMuted(true)
                            }
                            JitsiMeetActivity.launch(this@IncomingInvivationActivity,builder.build())
                            finish()
                        }catch (e : Exception){
                            Toast.makeText(this@IncomingInvivationActivity, e.message, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }else{
                        Toast.makeText(this@IncomingInvivationActivity, "Invitation Rejected", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }else{
                    Toast.makeText(this@IncomingInvivationActivity, response.message(), Toast.LENGTH_SHORT).show()
                    finish()
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(this@IncomingInvivationActivity, t.message, Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private val invitationResponseReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type : String? = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE)
            type?.let {
                if (type == Constants.REMOTE_MSG_INVITATION_CANCELLED) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            invitationResponseReceiver,
            IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(invitationResponseReceiver)
    }

}